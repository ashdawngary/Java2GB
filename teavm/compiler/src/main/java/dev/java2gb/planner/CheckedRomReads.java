package dev.java2gb.planner;

import java.util.*;
import org.teavm.model.*;
import org.teavm.model.instructions.*;
import org.teavm.model.util.UsageExtractor;

final class CheckedRomReads {
    private CheckedRomReads() {}
    static int optimize(Program program, java.util.function.Predicate<Variable> genericArray) {
        int fused = 0;
        Map<Variable, Instruction> definitions = new IdentityHashMap<>();
        Map<Variable, Integer> uses = new IdentityHashMap<>();
        for (BasicBlock block : program.getBasicBlocks()) {
            for (Phi phi : block.getPhis()) {
                for (Incoming incoming : phi.getIncomings()) uses.merge(incoming.getValue(), 1, Integer::sum);
            }
            for (Instruction insn : block) {
                UsageExtractor usage = new UsageExtractor();
                insn.acceptVisitor(usage);
                for (Variable used : usage.getUsedVariables()) uses.merge(used, 1, Integer::sum);
                if (insn instanceof AssignInstruction assign) definitions.put(assign.getReceiver(), insn);
                if (insn instanceof UnwrapArrayInstruction unwrap) definitions.put(unwrap.getReceiver(), insn);
                if (insn instanceof NullCheckInstruction check) definitions.put(check.getReceiver(), insn);
            }
        }
        for (Instruction insn : allInstructions(program)) {
            if (!(insn instanceof GetElementInstruction get) || !genericArray.test(get.getArray())) continue;
            Instruction previous = get.getPrevious();
            while (previous instanceof AssignInstruction || previous instanceof UnwrapArrayInstruction) {
                previous = previous.getPrevious();
            }
            if (!(previous instanceof BoundCheckInstruction bound) || bound.getArray() == null
                    || get.getIndex() != bound.getReceiver() || uses.getOrDefault(bound.getReceiver(), 0) != 1
                    || arrayOrigin(get.getArray(), definitions) != arrayOrigin(bound.getArray(), definitions)) continue;
            get.setIndex(bound.getIndex());
            bound.delete();
            fused++;
        }
        return fused;
    }

    private static Variable arrayOrigin(Variable value, Map<Variable, Instruction> definitions) {
        Set<Variable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        while (visited.add(value)) {
            Instruction definition = definitions.get(value);
            if (definition instanceof AssignInstruction assign) value = assign.getAssignee();
            else if (definition instanceof UnwrapArrayInstruction unwrap) value = unwrap.getArray();
            else if (definition instanceof NullCheckInstruction check) value = check.getValue();
            else return value;
        }
        return value;
    }

    private static List<Instruction> allInstructions(Program program) {
        List<Instruction> result = new ArrayList<>();
        for (BasicBlock block : program.getBasicBlocks()) for (Instruction insn : block) result.add(insn);
        return result;
    }
}
