package dev.java2gb.planner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import org.teavm.ast.InvocationExpr;
import org.teavm.backend.c.intrinsic.*;
import org.teavm.model.*;
import org.teavm.model.instructions.*;
import org.teavm.model.util.UsageExtractor;

final class RomArrays implements ClassHolderTransformer, Intrinsic {
    static final String INTRINSIC_CLASS = "dev.java2gb.planner.RomArrayAccess";
    private static final int HANDLE_BASE = 0x1000;
    private static final class Table {
        int id, bank;
        FieldReference field;
        ValueType item;
        int[] values;
        String initializer = "array literal";
        boolean required;
    }
    private static final class Flow {
        MethodReference method;
        Program program;
        BitSet[] points;
        BitSet returned = new BitSet();
        Flow(MethodReference method, Program program) {
            this.method = method;
            this.program = program;
            points = new BitSet[program.variableCount()];
            for (int i = 0; i < points.length; i++) {
                points[i] = new BitSet();
            }
        }
        BitSet at(Variable variable) { return points[variable.getIndex()]; }
    }
    private final String prefix;
    private final Map<FieldReference, Table> tables = new LinkedHashMap<>();
    private final Map<MethodReference, Table> literals = new LinkedHashMap<>();
    private final Set<String> readers = new TreeSet<>();
    private boolean genericAccess, fixedBounds;
    private final Set<String> memoryOperations = new TreeSet<>();
    private int readSites, fixedReadSites, fusedBounds, nonNullGuards;

    RomArrays(String prefix) { this.prefix = prefix; }

    @Override public void transformClass(ClassHolder cls, ClassHolderTransformerContext context) {
        if (!ApplicationScope.contains(prefix, cls.getName())) {
            return;
        }
        boolean convertedText = false;
        for (FieldHolder field : cls.getFields()) {
            var annotation = field.getAnnotations().get("dev.java2gb.api.RomData");
            if (annotation == null) {
                continue;
            }
            if (!field.hasModifier(ElementModifier.STATIC) || !field.hasModifier(ElementModifier.FINAL)
                    || !(field.getType() instanceof ValueType.Array array) || width(array.getItemType()) == 0) {
                throw new IllegalStateException("GBROM003: " + field.getReference()
                        + ": @RomData requires a static final boolean[], byte[], short[], char[], or int[]");
            }
            Table table = new Table();
            table.id = tables.size() + 1;
            if (table.id > 4095) {
                throw new IllegalStateException("GBROM003: too many ROM arrays");
            }
            table.field = field.getReference();
            table.item = ((ValueType.Array) field.getType()).getItemType();
            var bank = annotation.getValue("bank");
            table.bank = bank == null ? -1 : bank.getInt();
            if (table.bank < -1 || table.bank > 255) {
                throw new IllegalStateException("GBROM003: " + table.field + ": bank must be -1..255");
            }
            tables.put(table.field, table);
            extract(cls, table);
            convertedText |= !table.initializer.equals("array literal");
        }
        for (MethodHolder method : List.copyOf(cls.getMethods())) {
            if (method.getProgram() == null) { continue; }
            for (Instruction insn : instructions(method.getProgram())) {
                if (insn instanceof InvokeInstruction call && call.getMethod().getClassName().equals("dev.java2gb.api.RomText")) {
                    fail(method.getReference(), insn, "RomText calls require a literal @RomData field initializer");
                }
            }
        }
        if (convertedText) {
            removeUnusedText(cls.getMethod(new MethodDescriptor("<clinit>", ValueType.VOID)).getProgram());
        }
    }

    private void extract(ClassHolder cls, Table table) {
        MethodHolder initializer = cls.getMethod(new MethodDescriptor("<clinit>", ValueType.VOID));
        if (initializer == null || initializer.getProgram() == null) {
            throw new IllegalStateException("GBROM003: " + table.field + ": missing constant array initializer");
        }
        Program program = initializer.getProgram();
        List<Instruction> instructions = instructions(program);
        Map<Variable, Instruction> definitions = new IdentityHashMap<>();
        for (Instruction insn : instructions) {
            if (insn instanceof IntegerConstantInstruction c) { definitions.put(c.getReceiver(), insn); }
            if (insn instanceof ConstructArrayInstruction c) { definitions.put(c.getReceiver(), insn); }
            if (insn instanceof AssignInstruction a) { definitions.put(a.getReceiver(), insn); }
            if (insn instanceof UnwrapArrayInstruction u) { definitions.put(u.getReceiver(), insn); }
            if (insn instanceof CastIntegerInstruction c) { definitions.put(c.getReceiver(), insn); }
            if (insn instanceof StringConstantInstruction c) { definitions.put(c.getReceiver(), insn); }
            if (insn instanceof InvokeInstruction c && c.getReceiver() != null) { definitions.put(c.getReceiver(), insn); }
        }
        List<PutFieldInstruction> publications = new ArrayList<>();
        for (Instruction insn : instructions) {
            if (insn instanceof PutFieldInstruction put && put.getField().equals(table.field)) {
                publications.add(put);
            }
        }
        if (publications.size() != 1) {
            fail(initializer.getReference(), null, "exactly one constant initializer required for " + table.field);
        }
        PutFieldInstruction publication = publications.get(0);
        Instruction origin = origin(publication.getValue(), definitions, new HashSet<>());
        if (origin instanceof InvokeInstruction factory && factory.getMethod().getClassName().equals("dev.java2gb.api.RomText")) {
            extractText(cls, table, initializer, publication, factory, definitions, instructions);
            return;
        }
        if (!(origin instanceof ConstructArrayInstruction)) {
            fail(initializer.getReference(), publication, "literal array initializer required for " + table.field);
        }
        ConstructArrayInstruction allocation = (ConstructArrayInstruction) origin;
        Integer length = constant(allocation.getSize(), definitions, new HashSet<>());
        if (length == null || length < 0 || length > 16384 / width(table.item)) {
            fail(initializer.getReference(), allocation, "constant nonnegative array must fit one 16 KiB bank: " + table.field);
        }
        if (!allocation.getItemType().equals(table.item) || allocation.getBasicBlock() != publication.getBasicBlock()) {
            fail(initializer.getReference(), allocation, "initializer must be a straight-line array literal: " + table.field);
        }
        table.values = new int[length];
        BitSet initialized = new BitSet();
        List<Instruction> stores = new ArrayList<>();
        int start = instructions.indexOf(allocation), end = instructions.indexOf(publication);
        for (int i = 0; i < instructions.size(); i++) {
            Instruction insn = instructions.get(i);
            UsageExtractor uses = new UsageExtractor();
            insn.acceptVisitor(uses);
            boolean alias = false;
            for (Variable used : uses.getUsedVariables()) {
                alias |= origin(used, definitions, new HashSet<>()) == allocation;
            }
            if (!alias) {
                continue;
            }
            if (insn == publication) {
                continue;
            }
            if (i <= start || i >= end || insn.getBasicBlock() != allocation.getBasicBlock()) {
                fail(initializer.getReference(), insn, "initializer array escapes its literal before ROM publication: " + table.field);
            }
            if (insn instanceof UnwrapArrayInstruction || insn instanceof AssignInstruction) {
                continue;
            }
            if (!(insn instanceof PutElementInstruction)) {
                fail(initializer.getReference(), insn, "only constant element initialization is supported: " + table.field);
            }
            PutElementInstruction put = (PutElementInstruction) insn;
            Integer index = constant(put.getIndex(), definitions, new HashSet<>());
            Integer value = constant(put.getValue(), definitions, new HashSet<>());
            if (index == null || value == null || index < 0 || index >= length || initialized.get(index)) {
                fail(initializer.getReference(), insn, "unique constant index/value required in initializer: " + table.field);
            }
            initialized.set(index);
            table.values[index] = narrow(value, table.item);
            stores.add(insn);
        }
        replaceInitializer(cls, table, allocation, allocation.getReceiver(), stores);
    }

    private void extractText(ClassHolder cls, Table table, MethodHolder initializer,
            PutFieldInstruction publication, InvokeInstruction factory,
            Map<Variable, Instruction> definitions, List<Instruction> instructions) {
        String kind = factory.getMethod().getDescriptor().toString();
        boolean ascii = kind.equals("ascii(Ljava/lang/String;)[B");
        boolean bytes = kind.equals("bytes(Ljava/lang/String;)[B");
        boolean chars = kind.equals("chars(Ljava/lang/String;)[C");
        if ((!ascii && !chars && !bytes) || !factory.getMethod().getReturnType().equals(ValueType.arrayOf(table.item))
                || factory.getBasicBlock() != publication.getBasicBlock() || factory.getArguments().size() != 1) {
            fail(initializer.getReference(), factory, "use RomText.ascii(String) for byte[] or RomText.chars(String) for char[]");
        }
        Instruction source = origin(factory.getArguments().get(0), definitions, new HashSet<>());
        if (!(source instanceof StringConstantInstruction)) {
            fail(initializer.getReference(), factory, "RomText requires a compile-time String literal");
        }
        String text = ((StringConstantInstruction) source).getConstant();
        if (text.length() > 16384 / width(table.item)) {
            fail(initializer.getReference(), factory, "literal array must fit one 16 KiB ROM bank: " + table.field);
        }
        table.values = new int[text.length()];
        for (int i = 0; i < text.length(); i++) {
            int value = text.charAt(i);
            if (ascii && value > 127) {
                fail(initializer.getReference(), factory, "RomText.ascii contains non-ASCII code unit at index " + i);
            }
            if (bytes && value > 255) {
                fail(initializer.getReference(), factory, "RomText.bytes contains a value above 255 at index " + i);
            }
            table.values[i] = bytes ? (byte) value : value;
        }
        table.initializer = ascii ? "ASCII literal" : bytes ? "binary byte literal" : "UTF-16 literal";
        int start = instructions.indexOf(factory), end = instructions.indexOf(publication);
        for (int i = 0; i < instructions.size(); i++) {
            Instruction insn = instructions.get(i);
            UsageExtractor uses = new UsageExtractor();
            insn.acceptVisitor(uses);
            boolean alias = false;
            for (Variable used : uses.getUsedVariables()) {
                alias |= origin(used, definitions, new HashSet<>()) == factory;
            }
            if (!alias || insn == publication) { continue; }
            if (i <= start || i >= end || insn.getBasicBlock() != factory.getBasicBlock()
                    || !(insn instanceof AssignInstruction || insn instanceof UnwrapArrayInstruction)) {
                fail(initializer.getReference(), insn, "text initializer array escapes before ROM publication: " + table.field);
            }
        }
        replaceInitializer(cls, table, factory, factory.getReceiver(), List.of());
    }

    private void replaceInitializer(ClassHolder cls, Table table, Instruction allocation,
            Variable receiver, List<Instruction> stores) {
        MethodHolder factory = new MethodHolder("$gb$rom$array$" + table.id,
                ValueType.arrayOf(table.item));
        factory.getModifiers().addAll(EnumSet.of(ElementModifier.STATIC, ElementModifier.NATIVE));
        cls.addMethod(factory);
        literals.put(factory.getReference(), table);
        InvokeInstruction call = new InvokeInstruction();
        call.setType(InvocationType.SPECIAL);
        call.setMethod(factory.getReference());
        call.setReceiver(receiver);
        call.setLocation(allocation.getLocation());
        for (Instruction store : stores) { store.delete(); }
        allocation.replace(call);
    }

    private static void removeUnusedText(Program program) {
        boolean changed;
        do {
            changed = false;
            Set<Variable> used = Collections.newSetFromMap(new IdentityHashMap<>());
            for (BasicBlock block : program.getBasicBlocks()) {
                for (Phi phi : block.getPhis()) {
                    for (Incoming incoming : phi.getIncomings()) { used.add(incoming.getValue()); }
                }
                for (Instruction insn : block) {
                    UsageExtractor uses = new UsageExtractor();
                    insn.acceptVisitor(uses);
                    Collections.addAll(used, uses.getUsedVariables());
                }
            }
            for (Instruction insn : instructions(program)) {
                Variable receiver = insn instanceof StringConstantInstruction c ? c.getReceiver()
                        : insn instanceof AssignInstruction a ? a.getReceiver() : null;
                if (receiver != null && !used.contains(receiver)) { insn.delete(); changed = true; }
            }
        } while (changed);
    }

    private static Instruction origin(Variable variable, Map<Variable, Instruction> definitions, Set<Variable> active) {
        if (!active.add(variable)) { return null; }
        Instruction definition = definitions.get(variable);
        if (definition instanceof AssignInstruction a) { return origin(a.getAssignee(), definitions, active); }
        if (definition instanceof UnwrapArrayInstruction u) { return origin(u.getArray(), definitions, active); }
        return definition;
    }

    private static Integer constant(Variable variable, Map<Variable, Instruction> definitions, Set<Variable> active) {
        if (!active.add(variable)) { return null; }
        Instruction definition = definitions.get(variable);
        if (definition instanceof IntegerConstantInstruction c) { return c.getConstant(); }
        if (definition instanceof AssignInstruction a) { return constant(a.getAssignee(), definitions, active); }
        if (definition instanceof CastIntegerInstruction c) {
            Integer value = constant(c.getValue(), definitions, active);
            if (value == null) { return null; }
            return switch (c.getTargetType()) {
                case BYTE -> (int) (byte) (int) value;
                case SHORT -> (int) (short) (int) value;
                case CHAR -> (int) (char) (int) value;
            };
        }
        return null;
    }

    int nonNullGuardElisions() { return nonNullGuards; }
    boolean nonNullLiteral(MethodReference method) { return literals.containsKey(method); }

    void prepare(Map<MethodReference, Program> programs, ProvenNonNull nonnull) {
        for (Program program : programs.values()) {
            for (Instruction insn : instructions(program)) {
                if (insn instanceof InvokeInstruction call && readonlyMemory(call.getMethod())) {
                    memoryOperations.add(call.getMethod().getName());
                    genericAccess = true;
                }
            }
        }
        if (tables.isEmpty()) { return; }
        Map<MethodReference, Flow> flows = new LinkedHashMap<>();
        programs.forEach((method, program) -> flows.put(method, new Flow(method, program)));
        Map<FieldReference, BitSet> fields = new LinkedHashMap<>();
        boolean changed;
        do {
            changed = false;
            for (Flow flow : flows.values()) {
                for (BasicBlock block : flow.program.getBasicBlocks()) {
                    for (Phi phi : block.getPhis()) {
                        for (Incoming incoming : phi.getIncomings()) {
                            changed |= merge(flow.at(phi.getReceiver()), flow.at(incoming.getValue()));
                        }
                    }
                    for (Instruction insn : block) {
                        if (insn instanceof ConstructArrayInstruction array && !flow.at(array.getReceiver()).get(0)) {
                            flow.at(array.getReceiver()).set(0); changed = true;
                        }
                        if (insn instanceof NullConstantInstruction n && !flow.at(n.getReceiver()).get(0)) {
                            flow.at(n.getReceiver()).set(0); changed = true;
                        }
                        Variable receiver = null, source = null;
                        if (insn instanceof AssignInstruction a) { receiver = a.getReceiver(); source = a.getAssignee(); }
                        if (insn instanceof CastInstruction c) { receiver = c.getReceiver(); source = c.getValue(); }
                        if (insn instanceof NullCheckInstruction n) { receiver = n.getReceiver(); source = n.getValue(); }
                        if (insn instanceof UnwrapArrayInstruction u) { receiver = u.getReceiver(); source = u.getArray(); }
                        if (receiver != null) { changed |= merge(flow.at(receiver), flow.at(source)); }
                        if (insn instanceof PutFieldInstruction put && reference(put.getFieldType())) {
                            changed |= merge(fields.computeIfAbsent(put.getField(), k -> new BitSet()), flow.at(put.getValue()));
                        }
                        if (insn instanceof GetFieldInstruction get && reference(get.getFieldType())) {
                            changed |= merge(flow.at(get.getReceiver()), fields.getOrDefault(get.getField(), new BitSet()));
                        }
                        if (insn instanceof InvokeInstruction call) {
                            Table table = literals.get(call.getMethod());
                            if (table != null && call.getReceiver() != null) {
                                BitSet points = flow.at(call.getReceiver());
                                if (!points.get(table.id)) { points.set(table.id); changed = true; }
                                table.required = true;
                            }
                            Flow callee = flows.get(call.getMethod());
                            if (callee != null) {
                                if (call.getInstance() != null) { changed |= merge(callee.points[0], flow.at(call.getInstance())); }
                                for (int i = 0; i < call.getArguments().size(); i++) {
                                    if (reference(call.getMethod().parameterType(i))) {
                                        changed |= merge(callee.points[i + 1], flow.at(call.getArguments().get(i)));
                                    }
                                }
                                if (call.getReceiver() != null && reference(call.getMethod().getReturnType())) {
                                    changed |= merge(flow.at(call.getReceiver()), callee.returned);
                                }
                            } else if (table == null && call.getReceiver() != null && reference(call.getMethod().getReturnType())
                                    && !flow.at(call.getReceiver()).get(0)) {
                                flow.at(call.getReceiver()).set(0); changed = true;
                            }
                        }
                        if (insn instanceof ExitInstruction exit && exit.getValueToReturn() != null
                                && reference(flow.method.getReturnType())) {
                            changed |= merge(flow.returned, flow.at(exit.getValueToReturn()));
                        }
                    }
                }
            }
        } while (changed);
        for (Flow flow : flows.values()) {
            for (Instruction insn : instructions(flow.program)) { validate(flow, insn, flows); }
        }
        for (Flow flow : flows.values()) {
            fusedBounds += CheckedRomReads.optimize(flow.program,
                    array -> hasRom(flow.at(array)) && fixedTable(flow.at(array)) == null);
            for (Instruction insn : instructions(flow.program)) {
                if (insn instanceof GetElementInstruction get && hasRom(flow.at(get.getArray()))) {
                    String kind = get.getType().name().toLowerCase(Locale.ROOT);
                    if (!Set.of("byte", "short", "char", "int").contains(kind)) {
                        fail(flow.method, insn, "unsupported ROM array element type " + kind);
                    }
                    Table fixed = fixedTable(flow.at(get.getArray()));
                    if (fixed == null) { readers.add(kind); genericAccess = true; }
                    else {
                        fixedReadSites++;
                        if (nonnull.at(flow.method,get.getArray())) nonNullGuards++;
                    }
                    replace(insn, fixed == null ? "read_" + kind : "fixed_read_" + fixed.id + (nonnull.at(flow.method,get.getArray()) ? "_nonnull" : ""),
                            get.getReceiver(), get.getArray(), get.getIndex());
                    readSites++;
                } else if (insn instanceof ArrayLengthInstruction length && hasRom(flow.at(length.getArray()))) {
                    Table fixed = fixedTable(flow.at(length.getArray()));
                    if (fixed == null) { genericAccess = true; }
                    else if (nonnull.at(flow.method,length.getArray())) nonNullGuards++;
                    replace(insn, fixed == null ? "length" : "fixed_length_" + fixed.id + (nonnull.at(flow.method,length.getArray()) ? "_nonnull" : ""), length.getReceiver(), length.getArray());
                } else if (insn instanceof BoundCheckInstruction bound && bound.getArray() != null
                        && hasRom(flow.at(bound.getArray()))) {
                    Table fixed = fixedTable(flow.at(bound.getArray()));
                    if (fixed == null) { genericAccess = true; } else { fixedBounds = true; }
                    replace(insn, fixed == null ? "bounds" : "fixed_bounds_" + fixed.id,
                            bound.getReceiver(), bound.getArray(), bound.getIndex());
                }
            }
        }
    }

    private void validate(Flow flow, Instruction insn, Map<MethodReference, Flow> flows) {
        UsageExtractor uses = new UsageExtractor();
        insn.acceptVisitor(uses);
        BitSet aliases = new BitSet();
        for (Variable used : uses.getUsedVariables()) { aliases.or(flow.at(used)); }
        if (!hasRom(aliases)) { return; }
        if (insn instanceof PutElementInstruction put && hasRom(flow.at(put.getArray()))) {
            fail(flow.method, insn, "possible write through a ROM alias of " + describe(flow.at(put.getArray())));
        }
        if (insn instanceof InvokeInstruction call) {
            if (readonlyMemory(call.getMethod())) { return; }
            if (call.getType() == InvocationType.VIRTUAL || !flows.containsKey(call.getMethod())) {
                fail(flow.method, insn, "ROM reference passed to an unverified call " + call.getMethod() + ": " + describe(aliases));
            }
            return;
        }
        if (insn instanceof CastInstruction cast && !cast.isWeak()) {
            fail(flow.method, insn, "runtime type checks on opaque ROM arrays are unsupported");
        }
        if (insn instanceof GetFieldInstruction || insn instanceof PutFieldInstruction) {
            Variable instance = insn instanceof GetFieldInstruction get ? get.getInstance() : ((PutFieldInstruction) insn).getInstance();
            if (instance != null && hasRom(flow.at(instance))) {
                fail(flow.method, insn, "ROM arrays have no accessible object fields");
            }
            return;
        }
        if (!(insn instanceof AssignInstruction || insn instanceof CastInstruction
                || insn instanceof NullCheckInstruction || insn instanceof UnwrapArrayInstruction
                || insn instanceof GetElementInstruction || insn instanceof ArrayLengthInstruction
                || insn instanceof BoundCheckInstruction || insn instanceof ExitInstruction
                || insn instanceof BranchingInstruction || insn instanceof BinaryBranchingInstruction)) {
            fail(flow.method, insn, "unverified use of ROM reference: " + insn.getClass().getSimpleName());
        }
    }

    private String describe(BitSet ids) {
        return tables.values().stream().filter(t -> ids.get(t.id)).map(t -> t.field.toString()).toList().toString();
    }
    private static boolean hasRom(BitSet points) { return points.nextSetBit(1) >= 0; }
    private Table fixedTable(BitSet points) {
        if (points.cardinality() != 1 || points.get(0)) { return null; }
        int id = points.nextSetBit(1);
        return tables.values().stream().filter(t -> t.id == id && t.bank == 0).findFirst().orElse(null);
    }
    private static void replace(Instruction insn, String operation, Variable receiver, Variable... arguments) {
        ValueType[] signature = arguments.length == 1
                ? new ValueType[]{ValueType.object("java.lang.Object"), ValueType.INTEGER}
                : new ValueType[]{ValueType.object("java.lang.Object"), ValueType.INTEGER, ValueType.INTEGER};
        InvokeInstruction call = new InvokeInstruction();
        call.setType(InvocationType.SPECIAL);
        call.setMethod(new MethodReference(INTRINSIC_CLASS, operation, signature));
        call.setArguments(arguments);
        call.setReceiver(receiver);
        call.setLocation(insn.getLocation());
        insn.replace(call);
    }
    static boolean readonlyMemory(MethodReference method) {
        return method.getClassName().equals("dev.java2gb.api.RomMemory") && Set.of(
                "readIntLE([BI)I", "copyToVram([BIIII)V", "copyToPort([BIII)V", "copyToOam([BI)V")
                .contains(method.getDescriptor().toString());
    }
    boolean allow(MethodReference method) { return literals.containsKey(method) || method.getClassName().equals(INTRINSIC_CLASS) || readonlyMemory(method); }
    @Override public boolean canHandle(MethodReference method) { return allow(method); }
    @Override public void apply(IntrinsicContext context, InvocationExpr invocation) {
        Table table = literals.get(invocation.getMethod());
        if (table != null) {
            context.writer().print("((void*)(uintptr_t)").print(Integer.toString(HANDLE_BASE + table.id)).print(")");
            return;
        }
        String operation = invocation.getMethod().getName();
        if (operation.startsWith("fixed_")) {
            String[] parts = operation.split("_");
            int id = Integer.parseInt(parts[2]);
            Table fixed = tables.values().stream().filter(t -> t.id == id).findFirst().orElseThrow();
            boolean nonnull = parts.length > 3 && parts[3].equals("nonnull");
            if (parts[1].equals("length")) {
                context.writer().print(nonnull ? "((void)(" : "(teavm_nullCheck(");
                context.emit(invocation.getArguments().get(0));
                context.writer().print("), ").print(Integer.toString(fixed.values.length)).print("L)");
            } else if (parts[1].equals("bounds")) {
                context.writer().print("gb_rom_array_fixed_bounds(");
                context.emit(invocation.getArguments().get(0));
                context.writer().print(", ");
                context.emit(invocation.getArguments().get(1));
                context.writer().print(", ").print(Integer.toString(fixed.values.length)).print("L)");
            } else {
                context.writer().print(nonnull ? "((void)(" : "(teavm_nullCheck(");
                context.emit(invocation.getArguments().get(0));
                context.writer().print("), (int32_t)gb_rom_array_").print(Integer.toString(id)).print("[(uint16_t)(");
                context.emit(invocation.getArguments().get(1));
                context.writer().print(")])");
            }
            return;
        }
        String name = readonlyMemory(invocation.getMethod()) ? switch (operation) {
            case "readIntLE" -> "read4";
            case "copyToVram" -> "vram";
            case "copyToPort" -> "port";
            case "copyToOam" -> "oam";
            default -> throw new IllegalStateException(operation);
        } : operation;
        context.writer().print("gb_rom_array_").print(name).print("(");
        for (int i = 0; i < invocation.getArguments().size(); i++) {
            if (i > 0) { context.writer().print(", "); }
            context.emit(invocation.getArguments().get(i));
        }
        context.writer().print(")");
    }
    org.teavm.dependency.DependencyListener dependencyListener() {
        return new org.teavm.dependency.AbstractDependencyListener() {
            @Override public void methodReached(org.teavm.dependency.DependencyAgent agent,
                    org.teavm.dependency.MethodDependency method) {
                Table table = literals.get(method.getReference());
                if (table != null) {
                    table.required = true;
                    method.getResult().propagate(agent.getType(ValueType.arrayOf(table.item)));
                }
            }
        };
    }
    boolean required() { return !memoryOperations.isEmpty() || tables.values().stream().anyMatch(t -> t.required); }
    Map<String, Object> report() {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Table table : tables.values()) {
            if (table.required) {
                rows.add(Map.of("field", table.field.toString(), "id", table.id, "type", ValueType.arrayOf(table.item).toString(),
                        "length", table.values.length, "element_bytes", width(table.item), "payload_bytes", table.values.length * width(table.item),
                        "bank", table.bank, "handle", HANDLE_BASE + table.id, "initializer", table.initializer));
            }
        }
        return Map.of("required", required(), "arrays", rows, "runtime_allocations", 0,
                "memory_operations", memoryOperations,
                "sdcc_flags", memoryOperations.isEmpty() ? List.of() : List.of("--nolospre"),
                "payload_bytes", rows.stream().mapToInt(row -> (int) row.get("payload_bytes")).sum(),
                "read_sites", readSites, "fixed_read_sites", fixedReadSites, "fused_bounds_checks", fusedBounds,
                "analysis", "finite whole-program may-alias sets; unknown uses rejected");
    }
    void emit(Path output) throws IOException {
        Path directory = output.resolve("rom_arrays");
        if (Files.exists(directory)) {
            try (var paths = Files.walk(directory)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) { Files.delete(path); }
            }
        }
        Files.deleteIfExists(output.resolve("rom_arrays.h"));
        Files.deleteIfExists(output.resolve("rom_arrays_table.c"));
        if (!required()) { return; }
        Files.createDirectories(directory);
        StringBuilder descriptors = new StringBuilder("#include <gb/gb.h>\n");
        StringBuilder entries = new StringBuilder();
        for (Table table : tables.values()) {
            String name = "gb_rom_array_" + table.id;
            if (!table.required) {
                entries.append("{0,0,NULL},\n");
                continue;
            }
            String cType = cType(table.item);
            descriptors.append("extern const ").append(cType).append(' ').append(name).append("[];\nBANKREF_EXTERN(").append(name).append(")\n");
            entries.append('{').append(table.values.length).append(',').append(table.bank == 0 ? "0" : "BANK(" + name + ")")
                    .append(',').append(name).append("},\n");
            StringBuilder data = new StringBuilder("#include <gb/gb.h>\n");
            if (table.bank != 0) {
                data.append("#ifdef PROBE_BANKED_ROM\n#pragma bank ").append(table.bank < 0 ? 255 : table.bank).append("\n#endif\n");
            }
            data.append("BANKREF(").append(name).append(")\nconst ").append(cType).append(' ').append(name).append("[] = {\n");
            if (table.values.length == 0) { data.append('0'); }
            for (int i = 0; i < table.values.length; i++) {
                data.append(table.values[i]);
                if (table.item.equals(ValueType.INTEGER)) { data.append('L'); }
                data.append(',');
                if (i % 16 == 15) { data.append('\n'); }
            }
            Files.writeString(directory.resolve("array_" + table.id + ".c"), data.append("\n};\n").toString());
        }
        descriptors.append("static const GB_RomArray gb_rom_arrays[] = {\n").append(entries.length() == 0 ? "{0,0,NULL},\n" : entries).append("};\n");
        Files.writeString(output.resolve("rom_arrays_table.c"), descriptors.toString());
        StringBuilder declarations = new StringBuilder();
        for (Table table : tables.values()) {
            if (table.required && table.bank == 0) {
                declarations.append("extern const ").append(cType(table.item)).append(" gb_rom_array_").append(table.id).append("[];\n");
            }
        }
        if (genericAccess) { declarations.append("#define GB_ROM_ARRAY_GENERIC 1\n"); }
        if (fixedBounds) { declarations.append("#define GB_ROM_ARRAY_FIXED_BOUNDS 1\n"); }
        for (String operation : memoryOperations) { declarations.append("#define GB_ROM_MEMORY_").append(operation.toUpperCase(Locale.ROOT)).append(" 1\n"); }
        for (String reader : readers) { declarations.append("#define GB_ROM_ARRAY_READ_").append(reader.toUpperCase(Locale.ROOT)).append(" 1\n"); }
        Files.writeString(output.resolve("rom_arrays.h"), "#pragma once\n#include \"rom_memory.h\"\n#define GB_ROM_ARRAY_COUNT " + tables.size()
                + "\n#define GB_IS_ROM_ARRAY(p) ((uintptr_t)(p) > 0x1000U && (uintptr_t)(p) <= (0x1000U + GB_ROM_ARRAY_COUNT))\n"
                + "typedef struct { uint16_t length; uint8_t bank; const void* data; } GB_RomArray;\n"
                + "int32_t gb_rom_array_length(void* array);\nint32_t gb_rom_array_bounds(void* array, int32_t index);\n"
                + "int32_t gb_rom_array_read_byte(void* array, int32_t index);\nint32_t gb_rom_array_read_short(void* array, int32_t index);\n"
                + "int32_t gb_rom_array_read_char(void* array, int32_t index);\nint32_t gb_rom_array_read_int(void* array, int32_t index);\n"
                + "int32_t gb_rom_array_fixed_bounds(void* array, int32_t index, int32_t length);\n" + declarations);
    }
    private static List<Instruction> instructions(Program program) {
        List<Instruction> result = new ArrayList<>();
        for (BasicBlock block : program.getBasicBlocks()) { for (Instruction insn : block) { result.add(insn); } }
        return result;
    }
    private static boolean merge(BitSet target, BitSet source) { int size = target.cardinality(); target.or(source); return size != target.cardinality(); }
    private static boolean reference(ValueType type) { return type instanceof ValueType.Object || type instanceof ValueType.Array; }
    private static int width(ValueType type) {
        if (type.equals(ValueType.BOOLEAN) || type.equals(ValueType.BYTE)) { return 1; }
        if (type.equals(ValueType.SHORT) || type.equals(ValueType.CHARACTER)) { return 2; }
        return type.equals(ValueType.INTEGER) ? 4 : 0;
    }
    private static int narrow(int value, ValueType type) {
        if (type.equals(ValueType.BOOLEAN)) { return value & 1; }
        if (type.equals(ValueType.BYTE)) { return (byte) value; }
        if (type.equals(ValueType.SHORT)) { return (short) value; }
        if (type.equals(ValueType.CHARACTER)) { return (char) value; }
        return value;
    }
    private static String cType(ValueType type) {
        if (type.equals(ValueType.BOOLEAN) || type.equals(ValueType.BYTE)) { return "int8_t"; }
        if (type.equals(ValueType.SHORT)) { return "int16_t"; }
        return type.equals(ValueType.CHARACTER) ? "uint16_t" : "int32_t";
    }
    private static void fail(MethodReference method, Instruction insn, String message) {
        throw new IllegalStateException("GBROM003: " + method + " at "
                + (insn != null && insn.getLocation() != null ? insn.getLocation() : "unknown source") + ": " + message);
    }
}
