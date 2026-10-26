package dev.java2gb.planner;

import java.util.*;
import java.util.function.Predicate;
import org.teavm.model.*;
import org.teavm.model.instructions.*;

final class ProvenNonNull {
    private final Map<MethodReference, BitSet> facts = new LinkedHashMap<>();
    private final Set<FieldReference> fields = new LinkedHashSet<>();
    private int removed;

    ProvenNonNull(ListableClassReaderSource classes, Map<MethodReference, Program> programs,
                  Predicate<MethodReference> literal) {
        Set<FieldReference> earlyReads = new HashSet<>();
        Set<MethodReference> visited = new HashSet<>();
        Deque<MethodReference> pending = new ArrayDeque<>();
        programs.keySet().stream().filter(method -> method.getName().equals("<clinit>")).forEach(pending::add);
        while (!pending.isEmpty()) {
            MethodReference method = pending.remove();
            if (!visited.add(method)) continue;
            Program program = programs.get(method);
            if (program == null) continue;
            for (Instruction insn : instructions(program)) {
                if (insn instanceof GetFieldInstruction get && get.getInstance() == null) earlyReads.add(get.getField());
                if (insn instanceof InvokeInstruction call) pending.add(call.getMethod());
                if (insn instanceof InitClassInstruction init) pending.add(new MethodReference(init.getClassName(),"<clinit>",ValueType.VOID));
            }
        }
        Map<FieldReference, Integer> writes = new LinkedHashMap<>();
        for (Program program : programs.values()) for (Instruction insn : instructions(program)) {
            if (insn instanceof PutFieldInstruction put && put.getInstance() == null) writes.merge(put.getField(),1,Integer::sum);
        }
        for (var entry : programs.entrySet()) {
            if (!entry.getKey().getName().equals("<clinit>")) continue;
            BitSet initialized = analyze(entry.getValue(),Set.of(),literal);
            for (Instruction insn : instructions(entry.getValue())) {
                if (!(insn instanceof PutFieldInstruction put) || put.getInstance() != null
                        || earlyReads.contains(put.getField()) || writes.get(put.getField()) != 1
                        || !entry.getKey().getClassName().equals(put.getField().getClassName())
                        || !initialized.get(put.getValue().getIndex())) continue;
                ClassReader owner = classes.get(put.getField().getClassName());
                FieldReader field = owner == null ? null : owner.getField(put.getField().getFieldName());
                if (field != null && field.hasModifier(ElementModifier.FINAL) && field.hasModifier(ElementModifier.STATIC)) fields.add(put.getField());
            }
        }
        for (var entry : programs.entrySet()) {
            Program program = entry.getValue();
            BitSet nonnull = analyze(program,fields,literal);
            facts.put(entry.getKey(),nonnull);
            for (Instruction insn : instructions(program)) {
                if (insn instanceof NullCheckInstruction check && nonnull.get(check.getValue().getIndex())) {
                    AssignInstruction assign = new AssignInstruction();
                    assign.setAssignee(check.getValue());assign.setReceiver(check.getReceiver());assign.setLocation(check.getLocation());
                    check.replace(assign);removed++;
                }
            }
        }
    }

    private static BitSet analyze(Program program, Set<FieldReference> fields, Predicate<MethodReference> literal) {
        BitSet result = new BitSet(program.variableCount());
        boolean changed;
        do {
            int before = result.cardinality();
            for (BasicBlock block : program.getBasicBlocks()) {
                for (Phi phi : block.getPhis()) {
                    if (!phi.getIncomings().isEmpty() && phi.getIncomings().stream().allMatch(in -> result.get(in.getValue().getIndex()))) result.set(phi.getReceiver().getIndex());
                }
                for (Instruction insn : block) {
                    Variable proven = null;
                    if (insn instanceof ConstructInstruction make) proven = make.getReceiver();
                    else if (insn instanceof ConstructArrayInstruction make) proven = make.getReceiver();
                    else if (insn instanceof NullCheckInstruction check) proven = check.getReceiver();
                    else if (insn instanceof AssignInstruction assign && result.get(assign.getAssignee().getIndex())) proven = assign.getReceiver();
                    else if (insn instanceof UnwrapArrayInstruction unwrap && result.get(unwrap.getArray().getIndex())) proven = unwrap.getReceiver();
                    else if (insn instanceof CastInstruction cast && result.get(cast.getValue().getIndex())) proven = cast.getReceiver();
                    else if (insn instanceof GetFieldInstruction get && get.getInstance() == null && fields.contains(get.getField())) proven = get.getReceiver();
                    else if (insn instanceof InvokeInstruction call && call.getReceiver() != null && literal.test(call.getMethod())) proven = call.getReceiver();
                    if (proven != null) result.set(proven.getIndex());
                }
            }
            changed = before != result.cardinality();
        } while (changed);
        return result;
    }

    boolean at(MethodReference method, Variable variable) {
        BitSet methodFacts = facts.get(method);
        return methodFacts != null && methodFacts.get(variable.getIndex());
    }
    Map<String,Object> report(int requireNonNull, int romGuards) {
        return Map.of("removed_null_checks",removed,"proven_static_fields",fields.stream().map(Object::toString).toList(),
                "require_nonnull_lowered",requireNonNull,"elided_fixed_rom_null_guards",romGuards,"annotation_trust",false,
                "proof","SSA allocations, successful checks and literals; singly initialized final static fields with no initializer-time reads");
    }
    private static List<Instruction> instructions(Program program) {
        List<Instruction> result = new ArrayList<>();
        for (BasicBlock block : program.getBasicBlocks()) for (Instruction insn : block) result.add(insn);
        return result;
    }
}
