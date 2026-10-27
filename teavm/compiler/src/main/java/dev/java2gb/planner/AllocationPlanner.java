package dev.java2gb.planner;

import java.util.*;
import org.teavm.common.Graph;
import org.teavm.model.*;
import org.teavm.model.instructions.*;
import org.teavm.model.util.*;

final class AllocationPlanner {
    static final class Site {
        int id, slot = -1, bytes, length = -1, flattenedInto = -1, poolCapacity;
        ValueType type;
        MethodReference method;
        Instruction instruction;
        Variable receiver;
        boolean loop, escaped;
        String location;
    }
    static final class Slot {
        int id, bytes, poolCapacity;
        String method;
        List<Integer> sites = new ArrayList<>();
    }
    static final class Info {
        MethodReference ref;
        MethodHolder method;
        Program program;
        Graph cfg;
        BitSet loops;
        BitSet[] points;
        BitSet returned = new BitSet(), captured = new BitSet();
        List<Site> sites = new ArrayList<>();
        List<InvokeInstruction> calls = new ArrayList<>();
        List<Info> callees = new ArrayList<>();
        boolean once;
    }
    final List<Site> sites = new ArrayList<>();
    private final Set<String> nativeBindings = new TreeSet<>();
    private boolean arrayRuntime;
    private final List<Slot> slots = new ArrayList<>();
    final ListableClassHolderSource classes;
    final Map<MethodReference, Info> methods = new LinkedHashMap<>();
    private final String entry;
    private final int budget;
    private final Map<Instruction, Site> byInstruction = new IdentityHashMap<>();
    final Map<FieldReference, Map<Integer, BitSet>> graphFields = new LinkedHashMap<>();
    final Map<FieldReference, BitSet> staticReferences = new LinkedHashMap<>();
    final List<Map<String, Object>> flattening = new ArrayList<>();
    final List<Map<String, Object>> flatteningSkipped = new ArrayList<>();
    private final boolean flatten;
    private final StaticStrings strings;
    private final RomArrays romArrays;


    AllocationPlanner(ListableClassHolderSource classes, Map<MethodReference, Program> programs, String entry, int budget, boolean flatten, StaticStrings strings, RomArrays romArrays) {
        this.flatten = flatten; this.strings = strings;
        this.romArrays = romArrays;
        this.classes = classes; this.entry = entry; this.budget = budget;
        programs.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().toString())).forEach(e -> {
            Info info = new Info(); info.ref = e.getKey(); info.program = e.getValue();
            info.method = classes.get(info.ref.getClassName()).getMethod(info.ref.getDescriptor());
            info.cfg = ProgramUtils.buildControlFlowGraph(info.program);
            info.loops = cyclicBlocks(info.cfg);
            methods.put(info.ref, info);
        });
    }

    void plan() {
        if (budget < 0 || budget > 7168) throw new IllegalArgumentException("Object storage budget must be between 0 and 7168 bytes");
        discover();
        if (strings.required()) for (Info info : methods.values()) for (BasicBlock block : info.program.getBasicBlocks())
            for (Instruction insn : block) if (insn instanceof IsInstanceInstruction
                    || insn instanceof CastInstruction cast && !cast.isWeak())
                fail(info,insn,"GBSTR001","Runtime type checks/casts are unsupported with opaque static string handles");
        List<Info> ordered = new ArrayList<>();
        Set<Info> active = new HashSet<>(), done = new HashSet<>();
        for (Info info : methods.values()) order(info, active, done, ordered);
        analyzeGraph();
        if (flatten) {
            OwnedFieldFlattener flattener = new OwnedFieldFlattener(this);
            while (flattener.flattenOne()) {
                for (Site site : sites) if (site.flattenedInto < 0 && site.type instanceof ValueType.Object type) {
                    site.bytes = aligned(objectSize(type.getClassName(), methods.get(site.method), site.instruction));
                }
                analyzeGraph();
            }
        }
        for (Info info : methods.values()) info.once = info.ref.getName().equals("<clinit>")
                || info.ref.getClassName().equals(entry) && info.ref.getName().equals("main");
        boolean changed;
        do {
            changed = false;
            for (Info info : methods.values()) if (!info.once) {
                List<Map.Entry<Info, InvokeInstruction>> incoming = incoming(info);
                if (incoming.size() == 1 && incoming.get(0).getKey().once
                        && !incoming.get(0).getKey().loops.get(incoming.get(0).getValue().getBasicBlock().getIndex())) {
                    info.once = true; changed = true;
                }
            }
        } while (changed);
        for (Info info : methods.values()) assignSlots(info);
        int bytes = reservedBytes();
        if (bytes > budget) throw new IllegalStateException("GBMEM004: planned object storage needs " + bytes
                + " bytes, exceeding the " + budget + "-byte budget; no ROM emitted");
    }

    private void discover() {
        for (Info info : methods.values()) {
            boolean entryMain = info.ref.getClassName().equals(entry) && info.ref.getName().equals("main");
            if (info.method.hasModifier(ElementModifier.SYNCHRONIZED)) fail(info, null, "GBMEM005", "Synchronization is unsupported");
            for (BasicBlock block : info.program.getBasicBlocks()) {
                if (!block.getTryCatchBlocks().isEmpty()) fail(info, block.getFirstInstruction(), "GBMEM005", "Exception handlers are unsupported");
                for (Instruction insn : block) {
                    if (entryMain) {
                        UsageExtractor uses = new UsageExtractor(); insn.acceptVisitor(uses);
                        for (Variable v : uses.getUsedVariables()) if (v.getIndex() == 1)
                            fail(info, insn, "GBMEM005", "Command-line arguments are unavailable on Game Boy");
                    }
                    String kind = insn.getClass().getSimpleName();
                    if (kind.contains("Array") || kind.equals("GetElementInstruction")
                            || kind.equals("PutElementInstruction") || kind.equals("BoundCheckInstruction")) arrayRuntime = true;
                    if (insn instanceof ConstructInstruction c) {
                        Site s = site(info, insn, c.getReceiver());
                        s.type = ValueType.object(c.getType());
                        var annotation = classes.get(c.getType()).getAnnotations().get("probe.PoolCapacity");
                        if (annotation != null) {
                            s.poolCapacity = annotation.getValue("value").getInt();
                            if (s.poolCapacity < 1 || s.poolCapacity > 64)
                                fail(info, insn, "GBMEM006", "Pool capacity must be between 1 and 64 per allocation site");
                        }
                        s.bytes = aligned(objectSize(c.getType(), info, insn));
                    } else if (insn instanceof ConstructArrayInstruction a) {
                        int elementBytes = width(a.getItemType());
                        if (elementBytes == 0) {
                            fail(info, insn, "GBMEM005", "Only boolean[], byte[], short[], char[], and int[] storage is supported");
                        }
                        Integer length = constant(info.program, a.getSize(), new HashSet<>());
                        if (length == null || length < 0) fail(info, insn, "GBMEM002", "Array capacity must be a nonnegative compile-time constant");
                        if (length > (7168 - 12) / elementBytes) {
                            fail(info, insn, "GBMEM004", "Array exceeds Game Boy object storage capacity");
                        }
                        Site s = site(info, insn, a.getReceiver());
                        s.length = length;
                        s.type = ValueType.arrayOf(a.getItemType());
                        s.bytes = aligned(12 + length * elementBytes);
                    } else if (insn instanceof InvokeInstruction call) {
                        Info callee = methods.get(call.getMethod());
                        if (call.getType() == InvocationType.VIRTUAL) fail(info, insn, "GBMEM005", "Unresolved virtual/interface call cannot be analyzed");
                        if (callee != null) { info.calls.add(call); info.callees.add(callee); }
                        else if (!safeExternal(call)) fail(info, insn, "GBMEM005", "Unsupported external call " + call.getMethod());
                    } else if (insn instanceof InitClassInstruction init) {
                        Info callee = methods.get(new MethodReference(init.getClassName(), "<clinit>", ValueType.VOID));
                        if (callee != null) info.callees.add(callee);
                    } else if (kind.matches("(ConstructMultiArray|CloneArray|Raise|MonitorEnter|MonitorExit|StringConstant|ClassConstant|LongConstant|FloatConstant|DoubleConstant)Instruction")) {
                        fail(info, insn, "GBMEM005", "Unsupported operation " + kind);
                    }
                    if (insn instanceof GetFieldInstruction f) validateField(info, insn, f.getField(), f.getFieldType(), f.getInstance() != null);
                    if (insn instanceof PutFieldInstruction f) validateField(info, insn, f.getField(), f.getFieldType(), f.getInstance() != null);
                }
            }
        }
    }

    private Site site(Info info, Instruction instruction, Variable receiver) {
        Site s = new Site(); s.id = sites.size(); s.method = info.ref; s.instruction = instruction; s.receiver = receiver;
        s.location = location(info, instruction); s.loop = info.loops.get(instruction.getBasicBlock().getIndex());
        sites.add(s); info.sites.add(s); byInstruction.put(instruction, s); return s;
    }

    private void validateField(Info info, Instruction insn, FieldReference field, ValueType type, boolean instance) {
        String prefix = entry.substring(0, entry.lastIndexOf('.') + 1);
        if (!ApplicationScope.contains(prefix, field.getClassName())) fail(info, insn, "GBMEM005", "External class fields are unsupported: " + field);
        if (!reference(type) && width(type) == 0) fail(info, insn, "GBMEM005", "Unsupported field type " + field);
        if (!methods.keySet().stream().anyMatch(m -> m.getClassName().equals(field.getClassName()))
                && classes.get(field.getClassName()) == null) fail(info, insn, "GBMEM005", "Unknown field owner " + field);
    }

    private boolean safeExternal(InvokeInstruction call) {
        if (call.getMethod().getClassName().equals("java.lang.Object") && call.getMethod().getName().equals("<init>")) return true;
        MethodReference ref = call.getMethod();
        if (strings.allow(ref)) return true;
        if (romArrays.allow(ref)) return true;
        if (ref.getClassName().equals("dev.java2gb.api.GameBoyHardware")) {
            return Set.of("color()Z", "fast()V", "clock()I", "lcdOff()V", "read(I)I", "write(II)V",
                    "oamPacked(II)V", "vramPacked(II)V", "vramFill(III)V", "vramSpan(III)V", "vblank(I)V", "raster(III)V", "trap(I)V")
                    .contains(ref.getDescriptor().toString());
        }
        if (ref.getClassName().equals("dev.java2gb.api.GameBoySerial")) {
            return Set.of("open()V", "close()V", "arm(II)V", "reload(II)V", "schedule(I)V", "delay250()V", "read()I")
                    .contains(ref.getDescriptor().toString());
        }
        if (!ref.getClassName().equals("dev.java2gb.api.GameBoy")) return false;
        boolean allowed = Set.of("init()V", "tileRow(III)V", "show()V", "buttons()I", "moveSprite(II)V", "sprite(III)V", "spriteFlip(II)V", "hideSprites(II)V", "waitFrame()V", "report(I)V", "defineTile(IIII)V", "loadTileSet()V", "textTile(Ljava/lang/String;IZ)I", "textWidth(Ljava/lang/String;)I", "saveOpen()V", "saveRead(I)I", "saveWrite(II)V", "saveClose()V", "background(III)V", "backgroundPair(III)V", "backgroundColumn(III)V", "spriteTile(II)V", "display()V", "spriteChain(IIIIII)V", "scroll(II)V", "window(III)V", "hud(I)V", "viewport(I)V")
                .contains(ref.getDescriptor().toString());
        if (allowed) {
            nativeBindings.add(ref.getName());
            if (ref.getName().equals("textTile") || ref.getName().equals("textWidth")) strings.requireSupport();
        }
        return allowed;
    }

    private int objectSize(String name, Info info, Instruction insn) {
        String prefix = entry.substring(0, entry.lastIndexOf('.') + 1);
        if (!ApplicationScope.contains(prefix, name)) fail(info, insn, "GBMEM005", "Only application classes can be allocated: " + name);
        ClassHolder cls = classes.get(name);
        if (cls == null || !"java.lang.Object".equals(cls.getParent()) || !cls.getInterfaces().isEmpty()) {
            fail(info, insn, "GBMEM005", "Only plain classes extending Object are supported: " + name);
        }
        int bytes = 8;
        for (FieldHolder field : cls.getFields()) if (!field.hasModifier(ElementModifier.STATIC)) {
            int size = reference(field.getType()) ? 2 : width(field.getType());
            if (size == 0) fail(info, insn, "GBMEM005", "Unsupported instance field type: " + name + "." + field.getName());
            bytes += size;
        }
        return bytes;
    }
    private int width(ValueType t) {
        if (t.equals(ValueType.BOOLEAN) || t.equals(ValueType.BYTE)) return 1;
        if (t.equals(ValueType.SHORT) || t.equals(ValueType.CHARACTER)) return 2;
        if (t.equals(ValueType.INTEGER)) return 4;
        return 0;
    }
    private static boolean reference(ValueType t) { return t instanceof ValueType.Object || t instanceof ValueType.Array; }
    private static int aligned(int n) { return (n + 3) & ~3; }

    private void order(Info info, Set<Info> active, Set<Info> done, List<Info> result) {
        if (active.contains(info)) fail(info, null, "GBMEM003", "Recursive call graphs cannot use fixed method storage");
        if (!done.add(info)) return;
        active.add(info);
        for (Info next : info.callees) order(next, active, done, result);
        active.remove(info); result.add(info);
    }

    void analyzeGraph() {
        graphFields.clear(); staticReferences.clear();
        for (Info info : methods.values()) {
            info.returned.clear(); info.captured.clear();
            info.points = new BitSet[info.program.variableCount()];
            for (int i = 0; i < info.points.length; i++) info.points[i] = new BitSet(sites.size());
            for (Site site : info.sites) if (site.flattenedInto < 0) points(info, site.receiver).set(site.id);
        }
        boolean changed;
        do {
            changed = false;
            for (Info info : methods.values()) for (BasicBlock block : info.program.getBasicBlocks()) {
                for (Phi phi : block.getPhis()) for (Incoming in : phi.getIncomings())
                    changed |= merge(points(info, phi.getReceiver()), points(info, in.getValue()));
                for (Instruction insn : block) {
                    Variable receiver = null, source = null;
                    if (insn instanceof AssignInstruction a) { receiver = a.getReceiver(); source = a.getAssignee(); }
                    if (insn instanceof CastInstruction c) { receiver = c.getReceiver(); source = c.getValue(); }
                    if (insn instanceof NullCheckInstruction n) { receiver = n.getReceiver(); source = n.getValue(); }
                    if (insn instanceof UnwrapArrayInstruction u) { receiver = u.getReceiver(); source = u.getArray(); }
                    if (receiver != null) changed |= merge(points(info, receiver), points(info, source));
                    if (insn instanceof PutFieldInstruction put && reference(put.getFieldType())) {
                        if (put.getInstance() == null) changed |= merge(staticReferences.computeIfAbsent(put.getField(), k -> new BitSet()), points(info, put.getValue()));
                        else for (int owner : ids(points(info, put.getInstance()))) {
                            changed |= merge(graphFields.computeIfAbsent(put.getField(), k -> new LinkedHashMap<>())
                                    .computeIfAbsent(owner, k -> new BitSet()), points(info, put.getValue()));
                        }
                    }
                    if (insn instanceof GetFieldInstruction get && reference(get.getFieldType())) {
                        if (get.getInstance() == null) changed |= merge(points(info, get.getReceiver()), staticReferences.getOrDefault(get.getField(), new BitSet()));
                        else for (int owner : ids(points(info, get.getInstance()))) changed |= merge(points(info, get.getReceiver()),
                                graphFields.getOrDefault(get.getField(), Map.of()).getOrDefault(owner, new BitSet()));
                    }
                    if (insn instanceof InvokeInstruction call && methods.containsKey(call.getMethod())) {
                        Info callee = methods.get(call.getMethod());
                        if (call.getInstance() != null) changed |= merge(callee.points[0], points(info, call.getInstance()));
                        for (int i = 0; i < call.getArguments().size(); i++) if (reference(call.getMethod().parameterType(i)))
                            changed |= merge(callee.points[i + 1], points(info, call.getArguments().get(i)));
                        if (call.getReceiver() != null && reference(call.getMethod().getReturnType()))
                            changed |= merge(points(info, call.getReceiver()), callee.returned);
                    }
                    if (insn instanceof ExitInstruction exit && exit.getValueToReturn() != null && reference(info.ref.getReturnType()))
                        changed |= merge(info.returned, points(info, exit.getValueToReturn()));
                }
            }
        } while (changed);
        BitSet roots = new BitSet();
        for (BitSet stored : staticReferences.values()) roots.or(stored);
        for (Info info : methods.values()) {
            BitSet escaping = (BitSet)roots.clone(); escaping.or(info.returned);
            if (!info.method.hasModifier(ElementModifier.STATIC)) escaping.or(info.points[0]);
            for (int i = 0; i < info.ref.parameterCount(); i++) if (reference(info.ref.parameterType(i))) escaping.or(info.points[i+1]);
            info.captured = reachable(escaping);
        }
    }

    static List<Integer> ids(BitSet bits) {
        List<Integer> result = new ArrayList<>();
        for (int i = bits.nextSetBit(0); i >= 0; i = bits.nextSetBit(i + 1)) result.add(i);
        return result;
    }
    BitSet points(Info info, Variable variable) { return info.points[variable.getIndex()]; }
    private boolean merge(BitSet to, BitSet from) { int before = to.cardinality(); to.or(from); return to.cardinality() != before; }
    BitSet reachable(BitSet roots) {
        BitSet result = (BitSet)roots.clone();
        boolean changed;
        do {
            changed = false;
            for (Map<Integer, BitSet> edges : graphFields.values()) for (var edge : edges.entrySet())
                if (result.get(edge.getKey())) changed |= merge(result, edge.getValue());
        } while (changed);
        return result;
    }

    private List<Map.Entry<Info, InvokeInstruction>> incoming(Info target) {
        List<Map.Entry<Info, InvokeInstruction>> result = new ArrayList<>();
        for (Info caller : methods.values()) for (InvokeInstruction call : caller.calls) {
            if (call.getMethod().equals(target.ref)) result.add(Map.entry(caller, call));
        }
        return result;
    }

    private void assignSlots(Info info) {
        BitSet escaped = (BitSet)info.captured.clone(); escaped.or(info.returned);
        for (Site site : info.sites) if (site.flattenedInto < 0) {
            site.escaped = escaped.get(site.id);
            if (site.poolCapacity == 0 && site.escaped && (site.loop || !info.once)) fail(info, site.instruction, "GBMEM001",
                    "Allocation can escape a repeated execution; distinct live instances cannot share one fixed slot. "
                    + "A bounded pool or caller-specific planning is required");
        }
        BitSet[] conflicts = new BitSet[sites.size()];
        for (Site s : info.sites) conflicts[s.id] = new BitSet();
        var liveness = new LivenessAnalyzer(); liveness.analyze(info.program, info.ref.getDescriptor());
        var definitions = new DefinitionExtractor(); var usages = new UsageExtractor();
        for (BasicBlock block : info.program.getBasicBlocks()) {
            BitSet live = liveness.liveOut(block.getIndex());
            for (Instruction insn = block.getLastInstruction(); insn != null; insn = insn.getPrevious()) {
                addConflicts(info, origins(info, live), conflicts);
                insn.acceptVisitor(definitions);
                for (Variable v : definitions.getDefinedVariables()) live.clear(v.getIndex());
                insn.acceptVisitor(usages);
                for (Variable v : usages.getUsedVariables()) live.set(v.getIndex());
                BitSet current = origins(info, live);
                Site allocation = byInstruction.get(insn);
                if (allocation != null && allocation.flattenedInto < 0) {
                    if (allocation.poolCapacity == 0 && allocation.loop && current.get(allocation.id)) fail(info, insn, "GBMEM001",
                            "An earlier instance from this allocation site remains live across a loop iteration");
                    current.set(allocation.id);
                }
                addConflicts(info, current, conflicts);
            }
        }
        for (Site s : info.sites) if (s.flattenedInto < 0 && s.escaped) for (Site other : info.sites) if (other.flattenedInto < 0 && s != other) {
            conflicts[s.id].set(other.id); conflicts[other.id].set(s.id);
        }
        List<Slot> local = new ArrayList<>();
        List<Site> ordered = new ArrayList<>(info.sites.stream().filter(s -> s.flattenedInto < 0).toList());
        ordered.sort(Comparator.<Site>comparingInt(s -> -s.bytes).thenComparingInt(s -> s.id));
        for (Site site : ordered) {
            Slot chosen = null;
            for (Slot candidate : local) if (site.poolCapacity == 0 && candidate.poolCapacity == 0 && candidate.sites.stream().noneMatch(conflicts[site.id]::get)) { chosen = candidate; break; }
            if (chosen == null) {
                chosen = new Slot(); chosen.id = slots.size(); chosen.method = info.ref.toString();
                slots.add(chosen); local.add(chosen);
            }
            chosen.poolCapacity = site.poolCapacity;
            chosen.bytes = Math.max(chosen.bytes, site.bytes); chosen.sites.add(site.id); site.slot = chosen.id;
        }
    }
    private BitSet origins(Info info, BitSet live) {
        BitSet result = new BitSet();
        for (int v = live.nextSetBit(0); v >= 0; v = live.nextSetBit(v + 1)) result.or(info.points[v]);
        return reachable(result);
    }
    private void addConflicts(Info info, BitSet live, BitSet[] conflicts) {
        for (Site a : info.sites) if (a.flattenedInto < 0 && live.get(a.id)) for (Site b : info.sites) if (b.flattenedInto < 0 && a != b && live.get(b.id)) conflicts[a.id].set(b.id);
    }

    void lower() {
        for (Site site : sites) if (site.flattenedInto < 0) {
            var call = new InvokeInstruction(); call.setType(InvocationType.SPECIAL);
            call.setMethod(new MethodReference(StorageTarget.INTRINSIC_CLASS, "site_" + site.id, site.type));
            call.setReceiver(site.receiver); call.setLocation(site.instruction.getLocation());
            site.instruction.replace(call);
        }
    }

    Map<String, Object> report() {
        List<Map<String, Object>> allocations = new ArrayList<>();
        for (Site s : sites) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", s.id); row.put("slot", s.slot); row.put("bytes", s.bytes); row.put("type", s.type.toString());
            if (s.type instanceof ValueType.Array array) {
                row.put("length", s.length);
                row.put("element_bytes", width(array.getItemType()));
                row.put("payload_bytes", s.length * width(array.getItemType()));
            }
            row.put("location", s.location); row.put("in_loop", s.loop); row.put("escapes_method", s.escaped);
            row.put("flattened_into", s.flattenedInto < 0 ? null : s.flattenedInto);
            row.put("pool_capacity", s.poolCapacity);
            row.put("lifetime", s.flattenedInto >= 0 ? "embedded_fields" : s.poolCapacity > 0 ? "bounded_pool" : s.escaped ? "permanent" : s.loop ? "reused_each_iteration" : !methods.get(s.method).once ? "reused_each_call" : "liveness_planned");
            allocations.add(row);
        }
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("schema", 3); report.put("entry", entry);
        report.put("native_gameboy_methods", nativeBindings);
        report.put("array_runtime_required", arrayRuntime);
        report.put("analysis", "TeaVM SSA, field-sensitive graph reachability, whole-program aliases, CFG liveness");
        report.put("object_storage_budget_bytes", budget); report.put("reserved_object_bytes", reservedBytes());
        report.put("payload_object_bytes", slots.stream().mapToInt(s -> s.bytes * Math.max(1, s.poolCapacity)).sum());
        report.put("alignment_padding_bytes", cellCount() * 3); report.put("dynamic_heap_bytes", 0);
        report.put("pools_enabled", hasPools());
        report.put("pool_runtime_state_bytes", hasPools() ? cellCount() + 16 : 0);
        report.put("pool_exhaustion_policy", "fault_7_before_overwriting_live_objects; guard optional spawns in Java");
        report.put("pool_reclamation", "bounded tracing at every pooled allocation; live shadow-stack and static roots");
        report.put("allocations", allocations); report.put("slots", slots);
        List<Map<String, Object>> edges = new ArrayList<>();
        graphFields.forEach((field, owners) -> owners.forEach((owner, targets) -> {
            for (int child : ids(targets)) edges.add(Map.of("owner", owner, "field", field.toString(), "child", child));
        }));
        report.put("graph_edges", edges); report.put("flattening_enabled", flatten);
        report.put("flattened_fields", flattening); report.put("flattening_skipped", flatteningSkipped);
        return report;
    }
    boolean hasPools() { return slots.stream().anyMatch(s -> s.poolCapacity > 0); }
    private int cellCount() { return slots.stream().mapToInt(s -> Math.max(1, s.poolCapacity)).sum(); }
    private int reservedBytes() { return slots.stream().mapToInt(s -> (s.bytes + 3) * Math.max(1, s.poolCapacity)).sum(); }

    String entryDefinitions(org.teavm.backend.lowlevel.generate.NameProvider names) {
        StringBuilder out = new StringBuilder("");
        out.append("typedef char gb_int_width_check[sizeof(int32_t) == 4 ? 1 : -1];\n");
        for (Site s : sites) if (s.flattenedInto < 0) {
            if (s.type instanceof ValueType.Object type) {
                out.append("typedef char gb_layout_check_").append(s.id).append("[(((sizeof(")
                    .append(names.forClass(type.getClassName())).append(") + 3U) & ~3U) == ")
                    .append(s.bytes).append(") ? 1 : -1];\n");
            } else {
                out.append("typedef char gb_array_layout_check_").append(s.id)
                    .append("[sizeof(TeaVM_Array) == 12 ? 1 : -1];\n");
            }
        }
        if (hasPools()) {
            out.append("void gb_pool_static_roots(void) {\n");
            for (FieldReference field : staticReferences.keySet())
                out.append("gb_pool_mark(").append(names.forStaticField(field)).append(");\n");
            out.append("}\n");
        }
        var main = new MethodReference(entry, "main", ValueType.arrayOf(ValueType.object("java.lang.String")), ValueType.VOID);
        return out.append("void main(void) { ").append(names.forMethod(main))
            .append("(NULL); for (;;) gb_wait_frame(); }\n").toString();
    }

    String storageDefinitions() {
        StringBuilder out = new StringBuilder("");
        int cell = 0;
        for (Slot s : slots) for (int i = 0; i < Math.max(1, s.poolCapacity); i++)
            out.append("static union { uint32_t alignment; uint8_t bytes[").append(s.bytes + 3).append("]; } gb_cell_").append(cell++).append(";\n");
        out.append("#define GB_PLAN_BYTES ").append(reservedBytes()).append("\n");
        out.append("#define GB_PLAN_SLOT_COUNT ").append(slots.size()).append("\n");
        out.append("#define GB_PLAN_CELL_COUNT ").append(cellCount()).append("\n");
        out.append("static void* const gb_plan_slots[] = {");
        if (slots.isEmpty()) out.append("NULL");
        for (int i = 0; i < cellCount(); i++) out.append("gb_cell_").append(i).append(".bytes,");
        out.append("};\nstatic const uint16_t gb_plan_capacities[] = {");
        if (slots.isEmpty()) out.append("0");
        for (Slot s : slots) for (int i = 0; i < Math.max(1, s.poolCapacity); i++) out.append(s.bytes).append(',');
        out.append("};\nstatic const uint16_t gb_plan_first[] = {");
        if (slots.isEmpty()) out.append("0");
        cell = 0;
        for (Slot s : slots) { out.append(cell).append(','); cell += Math.max(1, s.poolCapacity); }
        out.append("};\nstatic const uint8_t gb_plan_pool_counts[] = {");
        if (slots.isEmpty()) out.append("0");
        for (Slot s : slots) out.append(s.poolCapacity).append(',');
        return out.append("};\n").toString();
    }

    private static Integer constant(Program program, Variable variable, Set<Integer> active) {
        if (!active.add(variable.getIndex())) return null;
        for (BasicBlock block : program.getBasicBlocks()) for (Instruction insn : block) {
            if (insn instanceof IntegerConstantInstruction c && c.getReceiver() == variable) return c.getConstant();
            if (insn instanceof AssignInstruction a && a.getReceiver() == variable) return constant(program, a.getAssignee(), active);
        }
        return null;
    }
    private static BitSet cyclicBlocks(Graph graph) {
        BitSet result = new BitSet();
        for (int start = 0; start < graph.size(); start++) {
            BitSet visited = new BitSet(); Deque<Integer> queue = new ArrayDeque<>();
            for (int next : graph.outgoingEdges(start)) queue.add(next);
            while (!queue.isEmpty()) {
                int node = queue.remove();
                if (node == start) { result.set(start); break; }
                if (visited.get(node)) continue;
                visited.set(node); for (int next : graph.outgoingEdges(node)) queue.add(next);
            }
        }
        return result;
    }
    private String location(Info info, Instruction insn) {
        return info.ref + " at " + (insn != null && insn.getLocation() != null ? insn.getLocation().toString() : "unknown source");
    }
    private void fail(Info info, Instruction insn, String code, String message) {
        throw new IllegalStateException(code + ": " + location(info, insn) + ": " + message);
    }
}
