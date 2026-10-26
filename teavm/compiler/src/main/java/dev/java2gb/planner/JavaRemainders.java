package dev.java2gb.planner;

import java.util.*;
import org.teavm.model.*;
import org.teavm.model.instructions.*;

final class JavaRemainders {
    private JavaRemainders() {}

    static int lower(Program program) {
        Map<Variable, Integer> constants = new IdentityHashMap<>();
        List<BinaryInstruction> remainders = new ArrayList<>();
        for (BasicBlock block : program.getBasicBlocks()) {
            for (Instruction instruction : block) {
                if (instruction instanceof IntegerConstantInstruction constant) {
                    constants.put(constant.getReceiver(), constant.getConstant());
                }
                if (instruction instanceof BinaryInstruction binary && binary.getOperandType() == NumericOperandType.INT
                        && binary.getOperation() == BinaryOperation.MODULO) {
                    remainders.add(binary);
                }
            }
        }
        int lowered = 0;
        for (BinaryInstruction remainder : remainders) {
            Integer divisor = constants.get(remainder.getSecondOperand());
            if (divisor != null && divisor > 0) { continue; }
            BinaryInstruction divide = new BinaryInstruction(BinaryOperation.DIVIDE, NumericOperandType.INT);
            divide.setFirstOperand(remainder.getFirstOperand()); divide.setSecondOperand(remainder.getSecondOperand());
            divide.setReceiver(program.createVariable()); divide.setLocation(remainder.getLocation());
            BinaryInstruction multiply = new BinaryInstruction(BinaryOperation.MULTIPLY, NumericOperandType.INT);
            multiply.setFirstOperand(divide.getReceiver()); multiply.setSecondOperand(remainder.getSecondOperand());
            multiply.setReceiver(program.createVariable()); multiply.setLocation(remainder.getLocation());
            BinaryInstruction subtract = new BinaryInstruction(BinaryOperation.SUBTRACT, NumericOperandType.INT);
            subtract.setFirstOperand(remainder.getFirstOperand()); subtract.setSecondOperand(multiply.getReceiver());
            subtract.setReceiver(remainder.getReceiver()); subtract.setLocation(remainder.getLocation());
            remainder.insertPrevious(divide); remainder.insertPrevious(multiply); remainder.insertPrevious(subtract);
            remainder.delete(); lowered++;
        }
        return lowered;
    }
}
