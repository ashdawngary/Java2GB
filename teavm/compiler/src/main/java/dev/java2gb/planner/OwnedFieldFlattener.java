package dev.java2gb.planner;

import java.util.*;
import org.teavm.common.GraphUtils;
import org.teavm.model.*;
import org.teavm.model.instructions.*;
import org.teavm.model.util.UsageExtractor;

final class OwnedFieldFlattener {
    private final AllocationPlanner plan;
    private record Binding(AllocationPlanner.Site owner, AllocationPlanner.Site child,
                           AllocationPlanner.Info info, PutFieldInstruction store) {}
    private String reason;

    OwnedFieldFlattener(AllocationPlanner plan) { this.plan = plan; }

    boolean flattenOne() {
        List<String> names = new ArrayList<>(plan.classes.getClassNames());
        Collections.sort(names);
        for (String name : names) {
            ClassHolder owner = plan.classes.get(name);
            for (FieldHolder field : new ArrayList<>(owner.getFields())) {
                if (field.hasModifier(ElementModifier.STATIC) || !(field.getType() instanceof ValueType.Object type)) continue;
                List<AllocationPlanner.Site> owners = plan.sites.stream().filter(s -> s.flattenedInto < 0
                        && s.type.equals(ValueType.object(name))).toList();
                if (owners.isEmpty()) continue;
                FieldReference ref = field.getReference();
                reason = null;
                ClassHolder child = plan.classes.get(type.getClassName());
                if (field.getLevel() != AccessLevel.PRIVATE || !field.hasModifier(ElementModifier.FINAL)) reason = "field is not private final";
                else if (child == null || !"java.lang.Object".equals(child.getParent())) reason = "child layout is not a plain application class";
                else for (FieldHolder f : child.getFields()) if (!f.hasModifier(ElementModifier.STATIC)
                        && (f.getType() instanceof ValueType.Object || f.getType() instanceof ValueType.Array)) reason = "child still contains reference fields";
                Map<Integer, Binding> bindings = reason == null ? bindings(ref, owners, type) : Map.of();
                if (reason == null) validateUses(ref, child, bindings);
                if (reason != null) {
                    String why = reason;
                    plan.flatteningSkipped.removeIf(row -> row.get("field").equals(ref.toString()));
                    plan.flatteningSkipped.add(Map.of("field", ref.toString(), "reason", why));
                    continue;
                }
                apply(owner, field, child, bindings);
                plan.flatteningSkipped.removeIf(row -> row.get("field").equals(ref.toString()));
                return true;
            }
        }
        return false;
    }

    private Map<Integer, Binding> bindings(FieldReference field, List<AllocationPlanner.Site> owners, ValueType childType) {
        Map<Integer, Binding> result = new LinkedHashMap<>();
        for (AllocationPlanner.Info info : plan.methods.values()) for (BasicBlock block : info.program.getBasicBlocks()) for (Instruction insn : block) {
            if (insn instanceof PutFieldInstruction put && put.getField().equals(field)) {
                int owner = single(info, put.getInstance()), child = single(info, put.getValue());
                if (owner < 0 || child < 0 || !plan.sites.get(child).type.equals(childType)) return reject("field assignment has nullable, shared, or unknown origins");
                var p = plan.sites.get(owner); var c = plan.sites.get(child);
                if (c.loop) return reject("child construction repeats inside control flow; separate lifetimes must be proven first");
                if (!p.method.equals(info.ref) || !c.method.equals(info.ref) || c.flattenedInto >= 0
                        || !dominates(info, p.instruction, c.instruction) || !dominates(info, c.instruction, put))
                    return reject("construction and assignment need a single dominating method context");
                if (result.put(owner, new Binding(p, c, info, put)) != null) return reject("field can be assigned more than once");
            }
        }
        for (var owner : owners) if (!result.containsKey(owner.id)) return reject("not every owner allocation has a proven non-null child assignment");
        for (Binding binding : result.values()) {
            for (var graphField : plan.graphFields.entrySet()) for (var edge : graphField.getValue().entrySet()) {
                if (edge.getValue().get(binding.child.id) && (!graphField.getKey().equals(field) || edge.getKey() != binding.owner.id))
                    return reject("child is shared by another owner or reference field");
            }
            for (BitSet roots : plan.staticReferences.values()) if (roots.get(binding.child.id)) return reject("child is retained by a static reference");
        }
        for (AllocationPlanner.Info info : plan.methods.values()) for (BasicBlock block : info.program.getBasicBlocks()) for (Instruction insn : block) {
            if (insn instanceof GetFieldInstruction get && get.getField().equals(field)) {
                Binding binding = result.get(single(info, get.getInstance()));
                if (binding == null || binding.info != info || !dominates(info, binding.store, get))
                    return reject("a field read is not dominated by its assignment in the same context");
            }
        }
        return result;
    }

    private void validateUses(FieldReference field, ClassHolder childClass, Map<Integer, Binding> bindings) {
        Map<Integer, Binding> children = new HashMap<>();
        for (Binding binding : bindings.values()) {
            if (children.put(binding.child.id, binding) != null) { reason = "child has more than one owner"; return; }
        }
        var usage = new UsageExtractor();
        for (AllocationPlanner.Info info : plan.methods.values()) for (BasicBlock block : info.program.getBasicBlocks()) {
            for (Phi phi : block.getPhis()) for (int child : children.keySet()) if (plan.points(info, phi.getReceiver()).get(child)) {
                if (single(info, phi.getReceiver()) != child) { reason = "child alias joins other instances"; return; }
                for (Incoming incoming : phi.getIncomings()) if (single(info, incoming.getValue()) != child) {
                    reason = "child alias may be null or refer to another object"; return;
                }
            }
            for (Instruction insn : block) {
                insn.acceptVisitor(usage);
                for (Variable used : usage.getUsedVariables()) for (int child : children.keySet()) if (plan.points(info, used).get(child)) {
                    Binding binding = children.get(child);
                    if (binding.info != info || single(info, used) != child) { reason = "child crosses a call boundary or aliases other instances"; return; }
                    boolean allowed = insn instanceof AssignInstruction || insn instanceof NullCheckInstruction;
                    if (insn instanceof GetFieldInstruction get) allowed = get.getInstance() == used
                            && get.getField().getClassName().equals(childClass.getName()) && primitive(get.getFieldType());
                    if (insn instanceof PutFieldInstruction put) allowed = put == binding.store && put.getValue() == used
                            || put.getInstance() == used && put.getField().getClassName().equals(childClass.getName()) && primitive(put.getFieldType());
                    if (insn instanceof InvokeInstruction call) allowed = call.getMethod().getClassName().equals("java.lang.Object")
                            && call.getMethod().getName().equals("<init>") && call.getInstance() == used;
                    if (!allowed) { reason = "child identity, escape, or opaque use is observable (" + insn.getClass().getSimpleName() + ")"; return; }
                }
            }
        }
    }

    private void apply(ClassHolder owner, FieldHolder field, ClassHolder childClass, Map<Integer, Binding> bindings) {
        Map<FieldReference, FieldReference> flattenedFields = new LinkedHashMap<>();
        List<String> newNames = new ArrayList<>();
        for (FieldHolder childField : childClass.getFields()) if (!childField.hasModifier(ElementModifier.STATIC)) {
            String base = "$gb$" + field.getName() + "$" + childField.getName(), name = base;
            for (int n = 1; owner.getField(name) != null; n++) name = base + "$" + n;
            FieldHolder replacement = new FieldHolder(name);
            replacement.setType(childField.getType()); replacement.setLevel(AccessLevel.PRIVATE);
            replacement.getModifiers().addAll(childField.getModifiers());
            owner.addField(replacement); flattenedFields.put(childField.getReference(), replacement.getReference()); newNames.add(name);
        }
        Map<Integer, Binding> children = new HashMap<>();
        bindings.values().forEach(binding -> children.put(binding.child.id, binding));
        for (AllocationPlanner.Info info : plan.methods.values()) for (BasicBlock block : info.program.getBasicBlocks()) {
            List<Instruction> instructions = new ArrayList<>(); block.forEach(instructions::add);
            for (Instruction insn : instructions) {
                if (insn instanceof GetFieldInstruction get && get.getField().equals(field.getReference())) {
                    AssignInstruction assign = new AssignInstruction(); assign.setAssignee(get.getInstance()); assign.setReceiver(get.getReceiver());
                    assign.setLocation(insn.getLocation()); insn.replace(assign);
                } else if (insn instanceof PutFieldInstruction put && put.getField().equals(field.getReference())) {
                    insn.delete();
                } else if (insn instanceof GetFieldInstruction get && get.getInstance() != null
                        && children.containsKey(single(info, get.getInstance())) && flattenedFields.containsKey(get.getField())) {
                    get.setField(flattenedFields.get(get.getField()));
                } else if (insn instanceof PutFieldInstruction put && put.getInstance() != null
                        && children.containsKey(single(info, put.getInstance())) && flattenedFields.containsKey(put.getField())) {
                    put.setField(flattenedFields.get(put.getField()));
                }
            }
        }
        for (Binding binding : bindings.values()) {
            AssignInstruction assign = new AssignInstruction(); assign.setAssignee(binding.owner.receiver); assign.setReceiver(binding.child.receiver);
            assign.setLocation(binding.child.instruction.getLocation()); binding.child.instruction.replace(assign);
            IntegerConstantInstruction zero = new IntegerConstantInstruction();
            zero.setReceiver(binding.info.program.createVariable()); zero.setConstant(0); zero.setLocation(assign.getLocation());
            assign.insertNext(zero);
            Instruction tail = zero;
            for (FieldReference replacement : flattenedFields.values()) {
                PutFieldInstruction clear = new PutFieldInstruction(); clear.setInstance(binding.child.receiver);
                clear.setField(replacement); clear.setFieldType(owner.getField(replacement.getFieldName()).getType());
                clear.setValue(zero.getReceiver()); clear.setLocation(assign.getLocation());
                tail.insertNext(clear); tail = clear;
            }
            binding.child.flattenedInto = binding.owner.id;
        }
        FieldReference original = field.getReference();
        owner.removeField(field);
        List<Map<String, Integer>> instances = new ArrayList<>();
        for (Binding b : bindings.values()) instances.add(Map.of("owner", b.owner.id, "child", b.child.id));
        plan.flattening.add(Map.of("field", original.toString(), "child_type", childClass.getName(), "instances", instances,
                "new_primitive_fields", newNames, "removed_child_header_bytes", 8 * bindings.size(), "removed_reference_bytes", 2 * bindings.size()));
    }

    private boolean dominates(AllocationPlanner.Info info, Instruction before, Instruction after) {
        if (before.getBasicBlock() == null || after.getBasicBlock() == null) return false;
        if (before.getBasicBlock() == after.getBasicBlock()) {
            for (Instruction next = before; next != null; next = next.getNext()) if (next == after) return true;
            return false;
        }
        return GraphUtils.buildDominatorTree(info.cfg).dominates(before.getBasicBlock().getIndex(), after.getBasicBlock().getIndex());
    }
    private int single(AllocationPlanner.Info info, Variable variable) {
        if (variable == null) return -1;
        BitSet points = plan.points(info, variable);
        return points.cardinality() == 1 ? points.nextSetBit(0) : -1;
    }
    private Map<Integer, Binding> reject(String why) { reason = why; return Map.of(); }
    private boolean primitive(ValueType type) { return !(type instanceof ValueType.Object || type instanceof ValueType.Array); }
}
