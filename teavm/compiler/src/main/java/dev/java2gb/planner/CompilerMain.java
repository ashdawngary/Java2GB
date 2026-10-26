package dev.java2gb.planner;

import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import org.teavm.vm.TeaVM;
import org.teavm.vm.TeaVMBuilder;
import org.teavm.vm.TeaVMOptimizationLevel;

public final class CompilerMain {
    public static void main(String[] args) throws Exception {
        try {
            compile(args);
        } catch (IllegalStateException | IllegalArgumentException error) {
            System.err.println("java2gb-teavm: " + error.getMessage());
            System.exit(1);
        }
    }

    private static void compile(String[] args) throws Exception {
        if (args.length < 3 || args.length > 5) {
            throw new IllegalArgumentException("Usage: CompilerMain <classes> <entry class> <output> [object-storage-budget] [--no-flatten]");
        }
        if (args.length == 5 && !args[4].equals("--no-flatten")) throw new IllegalArgumentException("Unknown option: " + args[4]);
        Path classes = Path.of(args[0]), output = Path.of(args[2]);
        Files.createDirectories(output);
        for (String file : new String[]{"storage-plan.json", "planned_storage.c", "planned_entry.c", "all.c"}) {
            Files.deleteIfExists(output.resolve(file));
        }
        String prefix = args[1].substring(0, args[1].lastIndexOf('.') + 1);
        try (var loader = new URLClassLoader(new java.net.URL[]{classes.toUri().toURL()}, CompilerMain.class.getClassLoader())) {
            var target = new StorageTarget(prefix, args[1], output, args.length >= 4 ? Integer.parseInt(args[3]) : 1024, args.length < 5 || !args[4].equals("--no-flatten"));
            var resources = new org.teavm.parsing.ClasspathResourceProvider(loader);
            var cache = new org.teavm.model.ReferenceCache();
            TeaVM vm = new TeaVMBuilder(target).setClassLoader(loader).setResourceProvider(resources).setReferenceCache(cache)
                    .setClassSource(new org.teavm.parsing.ClasspathClassHolderSource(resources, cache, loader))
                    .setObfuscated(true).build();
            vm.installPlugins();
            vm.add(target.nullness);
            vm.add(target.mutability);
            vm.add(target.romArrays);
            vm.add(target.strings);
            vm.add(target.strings.dependencyListener());
            vm.add(target.romArrays.dependencyListener());
            vm.setOptimizationLevel(TeaVMOptimizationLevel.FULL);
            vm.setEntryPoint(args[1]);
            vm.build(output.toFile(), "all.c");
            if (!vm.getProblemProvider().getSevereProblems().isEmpty()) {
                var text = new org.teavm.diagnostics.DefaultProblemTextConsumer();
                for (var problem : vm.getProblemProvider().getSevereProblems()) { problem.render(text); text.append("\n"); }
                throw new IllegalStateException("TeaVM compilation failed: " + text.getText());
            }
            if (!Files.exists(output.resolve("storage-plan.json"))) {
                throw new IllegalStateException("Compilation did not produce a validated storage plan");
            }
        }
    }
}
