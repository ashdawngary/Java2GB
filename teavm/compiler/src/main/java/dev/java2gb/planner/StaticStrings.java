package dev.java2gb.planner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import org.teavm.ast.InvocationExpr;
import org.teavm.backend.c.intrinsic.*;
import org.teavm.model.*;
import org.teavm.model.instructions.*;

final class StaticStrings implements ClassHolderTransformer, Intrinsic {
    private record Operation(String kind, String text) {}
    private final String prefix;
    private final Map<MethodReference, Operation> operations = new LinkedHashMap<>();
    private final Map<String, Integer> literals = new LinkedHashMap<>();
    private boolean required;
    private int nextMethod;
    StaticStrings(String prefix) { this.prefix = prefix; }
    private static boolean stringFamily(String name) {
        return name.equals("java.lang.String") || name.equals("java.lang.StringBuilder")
            || name.equals("java.lang.StringBuffer");
    }
    @Override public void transformClass(ClassHolder cls, ClassHolderTransformerContext context) {
        if (!cls.getName().startsWith(prefix)) return;
        for (MethodHolder method : List.copyOf(cls.getMethods())) {
            Program program = method.getProgram();
            if (program == null) continue;
            List<Instruction> instructions = new ArrayList<>();
            for (BasicBlock block : program.getBasicBlocks()) for (Instruction insn : block) instructions.add(insn);
            for (Instruction insn : instructions) {
                if (insn instanceof StringConstantInstruction literal) {
                    InvokeInstruction call = call(cls, new Operation("literal", literal.getConstant()),
                        new ValueType[]{ValueType.object("java.lang.String")}, List.of());
                    call.setReceiver(literal.getReceiver()); call.setLocation(insn.getLocation()); insn.replace(call);
                } else if (insn instanceof InvokeInstruction invoke && stringFamily(invoke.getMethod().getClassName())) {
                    MethodReference ref = invoke.getMethod();
                    String kind = switch (ref.getDescriptor().toString()) {
                        case "length()I" -> "length";
                        case "charAt(I)C" -> "charAt";
                        case "isEmpty()Z" -> "isEmpty";
                        default -> "unsupported:" + ref;
                    };
                    if (!ref.getClassName().equals("java.lang.String")) kind = "unsupported:" + ref;
                    List<Variable> arguments = new ArrayList<>(); List<ValueType> types = new ArrayList<>();
                    if (invoke.getInstance() != null) { arguments.add(invoke.getInstance()); types.add(ValueType.object(ref.getClassName())); }
                    arguments.addAll(invoke.getArguments()); types.addAll(Arrays.asList(ref.getParameterTypes()));
                    types.add(ref.getReturnType());
                    InvokeInstruction replacement = call(cls,new Operation(kind,null),types.toArray(ValueType[]::new),arguments);
                    replacement.setReceiver(invoke.getReceiver()); replacement.setLocation(insn.getLocation()); insn.replace(replacement);
                } else if (insn instanceof IsInstanceInstruction instance && instance.getType() instanceof ValueType.Object type && stringFamily(type.getClassName())) {
                    InvokeInstruction replacement = call(cls,new Operation("unsupported:string instanceof",null),
                        new ValueType[]{ValueType.object("java.lang.Object"),ValueType.BOOLEAN},List.of(instance.getValue()));
                    replacement.setReceiver(instance.getReceiver()); replacement.setLocation(insn.getLocation()); insn.replace(replacement);
                } else if (insn instanceof CastInstruction cast && cast.getTargetType() instanceof ValueType.Object type && stringFamily(type.getClassName())) {
                    InvokeInstruction replacement = call(cls,new Operation("unsupported:string cast",null),
                        new ValueType[]{ValueType.object("java.lang.Object"),cast.getTargetType()},List.of(cast.getValue()));
                    replacement.setReceiver(cast.getReceiver()); replacement.setLocation(insn.getLocation()); insn.replace(replacement);
                } else if (insn instanceof ConstructInstruction construct && stringFamily(construct.getType())) {
                    InvokeInstruction replacement = call(cls,new Operation("unsupported:new " + construct.getType(),null),
                        new ValueType[]{ValueType.object(construct.getType())},List.of());
                    replacement.setReceiver(construct.getReceiver()); replacement.setLocation(insn.getLocation()); insn.replace(replacement);
                }
            }
        }
    }
    private InvokeInstruction call(ClassHolder cls, Operation operation, ValueType[] signature, List<Variable> arguments) {
        MethodHolder method = new MethodHolder("$gb$string$" + nextMethod++, signature);
        method.getModifiers().addAll(EnumSet.of(ElementModifier.STATIC,ElementModifier.NATIVE));
        cls.addMethod(method); operations.put(method.getReference(),operation);
        InvokeInstruction call = new InvokeInstruction(); call.setType(InvocationType.SPECIAL);
        call.setMethod(method.getReference()); call.setArguments(arguments.toArray(Variable[]::new));
        return call;
    }
    boolean allow(MethodReference method) {
        Operation op = operations.get(method);
        if (op == null) return false;
        if (op.kind.startsWith("unsupported:")) throw new IllegalStateException("GBSTR001: only literal strings and length/charAt/isEmpty are supported; " + op.kind.substring(12));
        required = true;
        if (op.kind.equals("literal")) {
            if (op.text.length() > 8190) throw new IllegalStateException("GBSTR001: one UTF-16 string must fit a 16 KiB ROM bank (max 8190 code units)");
            literals.computeIfAbsent(op.text, k -> literals.size()+1);
            if (literals.size() > 4095) throw new IllegalStateException("GBSTR001: too many static string literals");
        }
        return true;
    }
    private boolean textQuery(MethodReference method) {
        return method.getClassName().equals("probe.GameBoy") && Set.of(
            "textTile(Ljava/lang/String;IZ)I", "textWidth(Ljava/lang/String;)I").contains(method.getDescriptor().toString());
    }
    @Override public boolean canHandle(MethodReference method) { return operations.containsKey(method) || textQuery(method); }
    @Override public void apply(IntrinsicContext context, InvocationExpr invocation) {
        if (textQuery(invocation.getMethod())) {
            context.includes().addInclude("<probe_bridge.h>");
            context.writer().print(invocation.getMethod().getName().equals("textTile") ? "probe_text_tile(" : "probe_text_width(");
            for (int i=0;i<invocation.getArguments().size();i++) {
                if (i>0) context.writer().print(", ");
                context.emit(invocation.getArguments().get(i));
            }
            context.writer().print(")");
            return;
        }
        Operation op = operations.get(invocation.getMethod());
        if (op.kind.equals("literal")) {
            context.writer().print("((void*)(uintptr_t)").print(Integer.toString(literals.get(op.text))).print(")");
        } else {
            if (op.kind.equals("isEmpty")) context.writer().print("(");
            context.writer().print(op.kind.equals("charAt") ? "gb_rom_string_char_at(" : "gb_rom_string_length(");
            for (int i=0;i<invocation.getArguments().size();i++) {
                if (i>0) context.writer().print(", ");
                context.emit(invocation.getArguments().get(i));
            }
            context.writer().print(")");
            if (op.kind.equals("isEmpty")) context.writer().print(" == 0)");
        }
    }
    org.teavm.dependency.DependencyListener dependencyListener() {
        return new org.teavm.dependency.AbstractDependencyListener() {
            @Override public void methodReached(org.teavm.dependency.DependencyAgent agent,
                    org.teavm.dependency.MethodDependency method) {
                Operation operation=operations.get(method.getReference());
                if (operation != null && operation.kind.equals("literal"))
                    method.getResult().propagate(agent.getType(ValueType.object("java.lang.String")));
            }
        };
    }
    void requireSupport() { required=true; }
    boolean required() { return required; }
    Map<String,Object> report() {
        return Map.of("required",required,"count",literals.size(),"utf16_payload_bytes",literals.keySet().stream().mapToInt(s->s.length()*2).sum(),
            "runtime_allocations",0,"encoding","UTF-16 code units; immutable ROM handles");
    }
    void emit(Path output) throws IOException {
        Path dataDir=output.resolve("rom_strings");
        if (Files.exists(dataDir)) {
            try (var paths=Files.walk(dataDir)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.delete(path);
            }
        }
        Files.deleteIfExists(output.resolve("rom_strings.h"));
        Files.deleteIfExists(output.resolve("rom_strings_table.c"));
        Files.deleteIfExists(output.resolve("rom_strings.json"));
        if (!required) return;
        StringBuilder table = new StringBuilder("#include <gb/gb.h>\n");
        StringBuilder entries = new StringBuilder();
        List<Map<String,Object>> manifest = new ArrayList<>();
        Files.createDirectories(dataDir);
        for (var entry : literals.entrySet()) {
            int id=entry.getValue(); String name="gb_rom_literal_"+id;
            manifest.add(Map.of("id",id,"code_units",entry.getKey().chars().toArray()));
            table.append("extern const uint16_t ").append(name).append("[];\nBANKREF_EXTERN(").append(name).append(")\n");
            entries.append("    {").append(entry.getKey().length()).append(", BANK(").append(name).append("), ").append(name).append("},\n");
            StringBuilder data = new StringBuilder("#include <gb/gb.h>\n#ifdef PROBE_BANKED_ROM\n#pragma bank 255\n#endif\nBANKREF(").append(name).append(")\nconst uint16_t ").append(name).append("[] = {");
            if (entry.getKey().isEmpty()) data.append("0");
            for (int i=0;i<entry.getKey().length();i++) { if(i>0)data.append(','); data.append((int)entry.getKey().charAt(i)); }
            Files.writeString(dataDir.resolve("literal_"+id+".c"),data.append("};\n").toString());
        }
        Files.writeString(output.resolve("rom_strings.json"),new com.google.gson.Gson().toJson(manifest));
        table.append("static const GB_RomString gb_rom_strings[] = {\n").append(entries);
        if (literals.isEmpty()) table.append("    {0,0,NULL},\n");
        Files.writeString(output.resolve("rom_strings_table.c"),table.append("};\n").toString());
        Files.writeString(output.resolve("rom_strings.h"),"#pragma once\n#define GB_ROM_STRING_COUNT "+literals.size()+"\n"
            +"typedef struct { uint16_t length; uint8_t bank; const uint16_t* data; } GB_RomString;\n"
            +"int32_t gb_rom_string_length(void* string);\nint32_t gb_rom_string_char_at(void* string,int32_t index);\n");
    }
}
