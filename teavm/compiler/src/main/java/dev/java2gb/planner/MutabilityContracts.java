package dev.java2gb.planner;

import java.util.*;
import org.teavm.model.*;
import org.teavm.model.instructions.*;
import org.teavm.model.util.ProgramUtils;

final class MutabilityContracts implements ClassHolderTransformer {
    private static final String ANNOTATION = "dev.java2gb.api.Mutable";
    private record Origin(String description, boolean mutable, boolean rom) {}
    private static final class Method {
        final MethodReference reference;
        final Program program;
        final boolean writesOwnedArrays;
        final boolean[] mutable;
        BitSet[] points;
        final BitSet returned = new BitSet();
        Method(MethodHolder method) {
            reference = method.getReference();
            program = method.getProgram() == null ? null : ProgramUtils.copy(method.getProgram());
            writesOwnedArrays = method.getAnnotations().get(ANNOTATION) != null;
            mutable = new boolean[method.parameterCount()];
            for (int i = 0; i < mutable.length; i++) {
                mutable[i] = method.parameterAnnotation(i).get(ANNOTATION) != null;
                if (mutable[i] && !(method.parameterType(i) instanceof ValueType.Array)) {
                    throw new IllegalStateException("GBMUT001: " + reference + ": @Mutable parameter "
                            + i + " must be an array");
                }
            }
        }
        BitSet at(Variable variable) { return points[variable.getIndex()]; }
    }
    private final String prefix;
    private final Map<MethodReference, Method> methods = new LinkedHashMap<>();
    private final Map<FieldReference, Integer> romFields = new LinkedHashMap<>();
    private final List<Origin> origins = new ArrayList<>();
    private int checkedMethods, writableParameters, declaredWriters;

    MutabilityContracts(String prefix) { this.prefix = prefix; }

    @Override public void transformClass(ClassHolder cls, ClassHolderTransformerContext context) {
        if (!ApplicationScope.contains(prefix, cls.getName())) { return; }
        for (FieldHolder field : cls.getFields()) {
            if (field.getAnnotations().get("dev.java2gb.api.RomData") != null) {
                romFields.put(field.getReference(), origins.size());
                origins.add(new Origin(field.getReference().toString(), false, true));
            }
        }
        for (MethodHolder method : cls.getMethods()) {
            methods.put(method.getReference(), new Method(method));
        }
    }

    void validate(Set<MethodReference> optimizedMethods) {
        Set<MethodReference> reachable = new LinkedHashSet<>();
        Deque<MethodReference> pending = new ArrayDeque<>(optimizedMethods);
        while (!pending.isEmpty()) {
            MethodReference reference = pending.remove();
            if (!reachable.add(reference)) { continue; }
            Method method = methods.get(reference);
            if (method == null) { continue; }
            MethodReference initializer = new MethodReference(reference.getClassName(), "<clinit>", ValueType.VOID);
            if (methods.containsKey(initializer) && !reachable.contains(initializer)) { pending.add(initializer); }
            if (method.program == null) { continue; }
            for (BasicBlock block : method.program.getBasicBlocks()) {
                for (Instruction insn : block) {
                    if (insn instanceof InvokeInstruction call) { pending.add(call.getMethod()); }
                    if (insn instanceof InitClassInstruction init) {
                        pending.add(new MethodReference(init.getClassName(), "<clinit>", ValueType.VOID));
                    }
                }
            }
        }
        List<Method> active = methods.values().stream()
                .filter(method -> method.program != null && reachable.contains(method.reference)).toList();
        for (Method method : active) {
            checkedMethods++;
            if (method.writesOwnedArrays) { declaredWriters++; }
            method.points = new BitSet[method.program.variableCount()];
            for (int i = 0; i < method.points.length; i++) { method.points[i] = new BitSet(); }
            for (int i = 0; i < method.mutable.length; i++) {
                if (method.reference.parameterType(i) instanceof ValueType.Array) {
                    method.points[i + 1].set(origins.size());
                    origins.add(new Origin(method.reference + " parameter " + i, method.mutable[i], false));
                    if (method.mutable[i]) { writableParameters++; }
                }
            }
        }
        Map<FieldReference, BitSet> fields = new LinkedHashMap<>();
        boolean changed;
        do {
            changed = false;
            for (Method method : active) {
                for (BasicBlock block : method.program.getBasicBlocks()) {
                    for (Phi phi : block.getPhis()) {
                        for (Incoming incoming : phi.getIncomings()) {
                            changed |= merge(method.at(phi.getReceiver()), method.at(incoming.getValue()));
                        }
                    }
                    for (Instruction insn : block) {
                        Variable receiver = null, source = null;
                        if (insn instanceof AssignInstruction a) { receiver = a.getReceiver(); source = a.getAssignee(); }
                        if (insn instanceof CastInstruction c) { receiver = c.getReceiver(); source = c.getValue(); }
                        if (insn instanceof NullCheckInstruction n) { receiver = n.getReceiver(); source = n.getValue(); }
                        if (insn instanceof UnwrapArrayInstruction u) { receiver = u.getReceiver(); source = u.getArray(); }
                        if (receiver != null) { changed |= merge(method.at(receiver), method.at(source)); }
                        if (insn instanceof PutFieldInstruction put && reference(put.getFieldType())) {
                            changed |= merge(fields.computeIfAbsent(put.getField(), key -> new BitSet()), method.at(put.getValue()));
                        }
                        if (insn instanceof GetFieldInstruction get && reference(get.getFieldType())) {
                            changed |= merge(method.at(get.getReceiver()), fields.getOrDefault(get.getField(), new BitSet()));
                            Integer rom = romFields.get(get.getField());
                            if (rom != null && !method.at(get.getReceiver()).get(rom)) {
                                method.at(get.getReceiver()).set(rom); changed = true;
                            }
                        }
                        if (insn instanceof InvokeInstruction call) {
                            Method callee = methods.get(call.getMethod());
                            if (callee != null && callee.points != null) {
                                if (call.getInstance() != null) { changed |= merge(callee.points[0], method.at(call.getInstance())); }
                                for (int i = 0; i < call.getArguments().size(); i++) {
                                    if (reference(call.getMethod().parameterType(i))) {
                                        changed |= merge(callee.points[i + 1], method.at(call.getArguments().get(i)));
                                    }
                                }
                                if (call.getReceiver() != null && reference(call.getMethod().getReturnType())) {
                                    changed |= merge(method.at(call.getReceiver()), callee.returned);
                                }
                            }
                        }
                        if (insn instanceof ExitInstruction exit && exit.getValueToReturn() != null
                                && reference(method.reference.getReturnType())) {
                            changed |= merge(method.returned, method.at(exit.getValueToReturn()));
                        }
                    }
                }
            }
        } while (changed);
        for (Method method : active) {
            for (BasicBlock block : method.program.getBasicBlocks()) {
                for (Instruction insn : block) {
                    if (insn instanceof PutElementInstruction put) {
                        BitSet aliases = method.at(put.getArray());
                        requireWritable(method, insn, aliases, "array element write");
                        if (!method.writesOwnedArrays && !method.reference.getName().equals("<clinit>")
                                && !mutableParameterAlias(method, put.getArray(), new HashSet<>())) {
                            fail(method, insn, "GBMUT001", "array writer must declare @Mutable on the method or the written array parameter");
                        }
                    }
                    if (insn instanceof InvokeInstruction call) {
                        Method callee = methods.get(call.getMethod());
                        for (int i = 0; i < call.getArguments().size(); i++) {
                            BitSet aliases = method.at(call.getArguments().get(i));
                            if (callee != null && callee.mutable[i]) {
                                requireWritable(method, insn, aliases, "passing an array to @Mutable parameter " + i + " of " + call.getMethod());
                            } else if (callee == null || callee.program == null) {
                                requireNoReadonlyEscape(method, insn, aliases, call.getMethod());
                            }
                        }
                        if (call.getInstance() != null && (callee == null || callee.program == null)) {
                            requireNoReadonlyEscape(method, insn, method.at(call.getInstance()), call.getMethod());
                        }
                    }
                }
            }
        }
    }

    private static boolean mutableParameterAlias(Method method, Variable variable, Set<Variable> active) {
        for (int i = 0; i < method.mutable.length; i++) {
            if (variable.getIndex() == i + 1 && method.mutable[i]) { return true; }
        }
        if (!active.add(variable)) { return false; }
        for (BasicBlock block : method.program.getBasicBlocks()) {
            for (Phi phi : block.getPhis()) {
                if (phi.getReceiver() == variable) {
                    for (Incoming incoming : phi.getIncomings()) {
                        if (!mutableParameterAlias(method, incoming.getValue(), new HashSet<>(active))) { return false; }
                    }
                    return !phi.getIncomings().isEmpty();
                }
            }
            for (Instruction insn : block) {
                Variable source = null;
                if (insn instanceof AssignInstruction a && a.getReceiver() == variable) { source = a.getAssignee(); }
                if (insn instanceof CastInstruction c && c.getReceiver() == variable) { source = c.getValue(); }
                if (insn instanceof NullCheckInstruction n && n.getReceiver() == variable) { source = n.getValue(); }
                if (insn instanceof UnwrapArrayInstruction u && u.getReceiver() == variable) { source = u.getArray(); }
                if (source != null) { return mutableParameterAlias(method, source, active); }
            }
        }
        return false;
    }

    private void requireWritable(Method method, Instruction insn, BitSet aliases, String operation) {
        for (int id = aliases.nextSetBit(0); id >= 0; id = aliases.nextSetBit(id + 1)) {
            Origin origin = origins.get(id);
            if (origin.rom) { fail(method, insn, "GBROM003", operation + " cannot mutate ROM " + origin.description); }
        }
        for (int id = aliases.nextSetBit(0); id >= 0; id = aliases.nextSetBit(id + 1)) {
            Origin origin = origins.get(id);
            if (!origin.mutable) {
                fail(method, insn, "GBMUT001", operation + " reaches read-only " + origin.description
                        + "; declare @Mutable on the parameter and its forwarding callers");
            }
        }
    }
    private void requireNoReadonlyEscape(Method method, Instruction insn, BitSet aliases, MethodReference call) {
        if (RomArrays.readonlyMemory(call)) { return; }
        BitSet readonly = new BitSet();
        for (int id = aliases.nextSetBit(0); id >= 0; id = aliases.nextSetBit(id + 1)) {
            if (!origins.get(id).mutable) { readonly.set(id); }
        }
        if (!readonly.isEmpty()) { requireWritable(method, insn, readonly, "unverified external call " + call); }
    }
    Map<String, Object> report() {
        return Map.of("array_parameters_readonly_by_default", true, "checked_before_inlining", true,
                "checked_methods", checkedMethods, "mutable_parameters", writableParameters,
                "declared_array_writer_methods", declaredWriters,
                "analysis", "finite whole-program may-alias sets on saved pre-optimization SSA");
    }
    private static boolean reference(ValueType type) { return type instanceof ValueType.Array || type instanceof ValueType.Object; }
    private static boolean merge(BitSet target, BitSet source) { int size = target.cardinality(); target.or(source); return size != target.cardinality(); }
    private static void fail(Method method, Instruction insn, String code, String message) {
        throw new IllegalStateException(code + ": " + method.reference + " at "
                + (insn.getLocation() == null ? "unknown source" : insn.getLocation()) + ": " + message);
    }
}
