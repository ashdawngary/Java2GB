package dev.java2gb.planner;

import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.teavm.ast.InvocationExpr;
import org.teavm.backend.c.CTarget;
import org.teavm.backend.c.generate.CNameProvider;
import org.teavm.backend.c.intrinsic.Intrinsic;
import org.teavm.backend.c.intrinsic.IntrinsicContext;
import org.teavm.model.ListableClassHolderSource;
import org.teavm.model.MethodReader;
import org.teavm.model.MethodReference;
import org.teavm.model.Program;
import org.teavm.model.util.ProgramUtils;
import org.teavm.model.util.RegisterAllocator;
import org.teavm.vm.BuildTarget;

final class StorageTarget extends CTarget {
    static final String INTRINSIC_CLASS = "dev.java2gb.planner.FixedAllocations";
    private final Map<MethodReference, Program> programs = new LinkedHashMap<>();
    private final String prefix, entry;
    private final Path output;
    private final int budget;
    private final boolean flatten;
    private final CNameProvider names;
    private AllocationPlanner planner;
    final StaticStrings strings;
    final RomArrays romArrays;
    final MutabilityContracts mutability;
    final NullnessContracts nullness;
    private ProvenNonNull nonnull;
    private int maskedShifts;
    private int loweredRemainders;
    private org.teavm.vm.TeaVMTargetController controller;

    StorageTarget(String prefix, String entry, Path output, int budget, boolean flatten) {
        this(prefix, entry, output, budget, flatten, new CNameProvider());
    }

    private StorageTarget(String prefix, String entry, Path output, int budget, boolean flatten, CNameProvider names) {
        super(names);
        this.names = names;
        strings = new StaticStrings(prefix);
        addIntrinsic(context -> strings);
        romArrays = new RomArrays(prefix);
        addIntrinsic(context -> romArrays);
        mutability = new MutabilityContracts(prefix);
        nullness = new NullnessContracts(prefix);
        this.flatten = flatten;
        this.prefix = prefix; this.entry = entry; this.output = output; this.budget = budget;
        addIntrinsic(context -> new Intrinsic() {
            public boolean canHandle(MethodReference method) {
                return method.getClassName().equals(INTRINSIC_CLASS);
            }
            public void apply(IntrinsicContext context, InvocationExpr invocation) {
                int id = Integer.parseInt(invocation.getMethod().getName().substring(5));
                var site = planner.sites.get(id);
                context.includes().includeType(site.type);
                context.writer().print("gb_plan_allocate(").print(Integer.toString(site.slot)).print(", &")
                        .print(context.names().forClassInstance(site.type)).print(", ").print(Integer.toString(site.length)).print(")");
            }
        });
    }

    @Override public void setController(org.teavm.vm.TeaVMTargetController controller) {
        this.controller = controller;
        super.setController(controller);
    }

    @Override public org.teavm.model.optimization.InliningFilterFactory getInliningFilter() {
        return caller -> callee -> {
            var cls = controller.getUnprocessedClassSource().get(callee.getClassName());
            var method = cls != null ? cls.getMethod(callee.getDescriptor()) : null;
            return method == null || method.getAnnotations().get("probe.NoInline") == null;
        };
    }

    @Override public void afterOptimizations(Program program, MethodReader method) {
        if (method.getOwnerName().startsWith(prefix)) {
            programs.put(method.getReference(), ProgramUtils.copy(program));
        }
        super.afterOptimizations(program, method);
    }

    private Map<String,Object> report() {
        Map<String,Object> result = planner.report();
        result.put("static_strings",strings.report());
        result.put("rom_arrays",romArrays.report());
        result.put("mutability",mutability.report());
        result.put("nullness",nonnull.report(nullness.lowered(),romArrays.nonNullGuardElisions()));
        result.put("integer_semantics",Map.of("masked_int_shift_counts",maskedShifts,
                "corrected_signed_remainders",loweredRemainders));
        return result;
    }

    @Override public void emit(ListableClassHolderSource classes, BuildTarget buildTarget, String name) throws IOException {
        mutability.validate(programs.keySet());
        nonnull = new ProvenNonNull(classes,programs,romArrays::nonNullLiteral);
        romArrays.prepare(programs,nonnull);
        planner = new AllocationPlanner(classes, programs, entry, budget, flatten, strings, romArrays);
        planner.plan();
        planner.lower();
        for (var item : programs.entrySet()) {
            var method = classes.get(item.getKey().getClassName()).getMethod(item.getKey().getDescriptor());
            Program program = item.getValue();
            maskedShifts += JavaShifts.lower(program);
            loweredRemainders += JavaRemainders.lower(program);
            method.setProgram(program);
            super.afterOptimizations(program, method);
            new RegisterAllocator(variableCategoryProvider()).allocateRegisters(method.getReference(), program, false);
        }
        super.emit(classes, buildTarget, name);
        PrimitiveArrayMetadata.emit(output, planner, names);
        Files.writeString(output.resolve("storage-plan.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report()));
        strings.emit(output);
        romArrays.emit(output);
        Files.writeString(output.resolve("planned_storage.c"), planner.storageDefinitions());
        Files.writeString(output.resolve("planned_entry.c"), planner.entryDefinitions(names));
    }
}
