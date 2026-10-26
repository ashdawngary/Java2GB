package dev.java2gb.planner;

import java.util.*;
import org.teavm.model.*;
import org.teavm.model.instructions.*;

final class JavaShifts {
    private JavaShifts() {}

    static int lower(Program program) {
        Map<Variable, Integer> constants = new IdentityHashMap<>();
        List<BinaryInstruction> shifts = new ArrayList<>();
        for (BasicBlock block : program.getBasicBlocks()) {
            for (Instruction instruction : block) {
                if (instruction instanceof IntegerConstantInstruction constant) {
                    constants.put(constant.getReceiver(), constant.getConstant());
                }
                if (instruction instanceof BinaryInstruction binary && binary.getOperandType() == NumericOperandType.INT
                        && (binary.getOperation() == BinaryOperation.SHIFT_LEFT
                        || binary.getOperation() == BinaryOperation.SHIFT_RIGHT
                        || binary.getOperation() == BinaryOperation.SHIFT_RIGHT_UNSIGNED)) {
                    shifts.add(binary);
                }
            }
        }
        int lowered = 0;
        Map<BasicBlock, Map<Variable, Variable>> masked = new IdentityHashMap<>();
        for (BinaryInstruction shift : shifts) {
            Integer count = constants.get(shift.getSecondOperand());
            if (count != null && count >= 0 && count < 32) { continue; }
            Map<Variable, Variable> blockMasks = masked.computeIfAbsent(shift.getBasicBlock(), key -> new IdentityHashMap<>());
            Variable originalCount = shift.getSecondOperand();
            Variable existing = blockMasks.get(originalCount);
            if (existing != null) {
                shift.setSecondOperand(existing); lowered++; continue;
            }
            IntegerConstantInstruction mask = new IntegerConstantInstruction();
            mask.setConstant(count != null ? count & 31 : 31);
            mask.setReceiver(program.createVariable()); mask.setLocation(shift.getLocation());
            shift.insertPrevious(mask);
            if (count != null) {
                shift.setSecondOperand(mask.getReceiver());
            } else {
                BinaryInstruction and = new BinaryInstruction(BinaryOperation.AND, NumericOperandType.INT);
                and.setFirstOperand(shift.getSecondOperand()); and.setSecondOperand(mask.getReceiver());
                and.setReceiver(program.createVariable()); and.setLocation(shift.getLocation());
                shift.insertPrevious(and); shift.setSecondOperand(and.getReceiver());
            }
            blockMasks.put(originalCount,shift.getSecondOperand());
            lowered++;
        }
        return lowered;
    }
}
