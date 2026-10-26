package dev.java2gb.planner;

import com.sun.source.tree.*;
import com.sun.source.util.*;
import java.io.File;
import java.nio.file.*;
import java.util.*;
import javax.lang.model.element.*;
import javax.tools.*;

public final class FinalBindings {
    private record Binding(VariableTree tree, Element element, long position, boolean initialized) {}
    private record Edit(int position, String text) {}

    public static void main(final String[] args) throws Exception {
        if (args.length < 4 || !Set.of("--check", "--fix").contains(args[0])) {
            throw new IllegalArgumentException("FinalBindings <--check|--fix> <scope paths> <classpath> <sources...>");
        }
        final boolean fix = !args[0].equals("--check");
        final List<Path> scopes = Arrays.stream(args[1].split(java.util.regex.Pattern.quote(File.pathSeparator)))
                .map(path -> Path.of(path).toAbsolutePath().normalize()).toList();
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        int violations = 0, files = 0, mixedDeclarations = 0;
        try (final StandardJavaFileManager manager = compiler.getStandardFileManager(diagnostics, null, null)) {
            final List<String> options = List.of("--release", "8", "-proc:none", "-Xlint:-options", "-classpath", args[2]);
            final JavacTask task = (JavacTask) compiler.getTask(null, manager, diagnostics, options, null,
                    manager.getJavaFileObjectsFromStrings(Arrays.asList(args).subList(3, args.length)));
            final List<CompilationUnitTree> units = new ArrayList<>();
            task.parse().forEach(units::add);
            task.analyze();
            for (final Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
                if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
                    throw new IllegalStateException("Cannot check invalid Java: " + diagnostic);
                }
            }
            final Trees trees = Trees.instance(task);
            final SourcePositions positions = trees.getSourcePositions();
            for (final CompilationUnitTree unit : units) {
                final Path path = Path.of(unit.getSourceFile().toUri()).toAbsolutePath().normalize();
                if (scopes.stream().noneMatch(path::startsWith)) { continue; }
                files++;
                final Map<Element, Integer> writes = new HashMap<>();
                final List<Binding> bindings = new ArrayList<>();
                final Map<Integer, Edit> edits = new TreeMap<>(Comparator.reverseOrder());
                new TreePathScanner<Void, Void>() {
                    private void written(final ExpressionTree expression) {
                        final Element element = trees.getElement(new TreePath(getCurrentPath(), expression));
                        if (element != null) { writes.merge(element, 1, Integer::sum); }
                    }
                    @Override public Void visitAssignment(final AssignmentTree node, final Void unused) {
                        written(node.getVariable()); return super.visitAssignment(node, unused);
                    }
                    @Override public Void visitCompoundAssignment(final CompoundAssignmentTree node, final Void unused) {
                        written(node.getVariable()); return super.visitCompoundAssignment(node, unused);
                    }
                    @Override public Void visitUnary(final UnaryTree node, final Void unused) {
                        if (Set.of(Tree.Kind.PREFIX_INCREMENT, Tree.Kind.PREFIX_DECREMENT,
                                Tree.Kind.POSTFIX_INCREMENT, Tree.Kind.POSTFIX_DECREMENT).contains(node.getKind())) {
                            written(node.getExpression());
                        }
                        return super.visitUnary(node, unused);
                    }
                    @Override public Void visitVariable(final VariableTree node, final Void unused) {
                        final Element element = trees.getElement(getCurrentPath());
                        if (element != null && Set.of(ElementKind.LOCAL_VARIABLE, ElementKind.PARAMETER,
                                ElementKind.EXCEPTION_PARAMETER, ElementKind.RESOURCE_VARIABLE).contains(element.getKind())) {
                            final Tree parent = getCurrentPath().getParentPath().getLeaf();
                            bindings.add(new Binding(node, element, positions.getStartPosition(unit, node.getType()),
                                    node.getInitializer() != null || element.getKind() != ElementKind.LOCAL_VARIABLE
                                            || parent instanceof EnhancedForLoopTree));
                        }
                        return super.visitVariable(node, unused);
                    }
                }.scan(unit, null);
                {
                    final Map<Long, List<Binding>> groups = new LinkedHashMap<>();
                    for (final Binding binding : bindings) {
                        if (binding.position >= 0) { groups.computeIfAbsent(binding.position, key -> new ArrayList<>()).add(binding); }
                    }
                    for (final List<Binding> group : groups.values()) {
                        final List<Binding> stable = group.stream()
                                .filter(b -> b.initialized && writes.getOrDefault(b.element, 0) == 0).toList();
                        if (stable.isEmpty()) { continue; }
                        if (stable.size() != group.size()) {
                            mixedDeclarations++;
                            System.err.println("GBFINAL001: " + path + ":" + unit.getLineMap().getLineNumber(stable.get(0).position)
                                    + ": split mixed declaration to make " + stable.get(0).tree.getName() + " final");
                            continue;
                        }
                        final Binding binding = group.get(0);
                        if (binding.tree.getModifiers().getFlags().contains(Modifier.FINAL)) { continue; }
                        violations++;
                        if (fix) { edits.put((int) binding.position, new Edit((int) binding.position, "final ")); }
                        else {
                            System.err.println("GBFINAL001: " + path + ":" + unit.getLineMap().getLineNumber(binding.position)
                                    + ": stable binding " + binding.tree.getName() + " must be final");
                        }
                    }
                }
                if (!edits.isEmpty()) {
                    final StringBuilder source = new StringBuilder(Files.readString(path));
                    for (final Edit edit : edits.values()) { source.insert(edit.position, edit.text); }
                    Files.writeString(path, source.toString());
                }
            }
        }
        System.out.println("Final bindings: " + files + " source files; " + violations
                + (fix ? " bindings fixed" : " missing final bindings") + "; " + mixedDeclarations + " mixed declarations");
        if ((!fix && violations != 0) || mixedDeclarations != 0) { System.exit(1); }
    }
}
