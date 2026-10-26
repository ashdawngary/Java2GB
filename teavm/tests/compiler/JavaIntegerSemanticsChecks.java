package dev.java2gb.planner;

import java.util.*;
import org.teavm.model.*;
import org.teavm.model.instructions.*;

public final class JavaIntegerSemanticsChecks {
    private static int cases;
    private static void check(boolean value) {
        cases++;
        if (!value) { throw new AssertionError("Integer lowering case " + cases); }
    }
    private record Fixture(Program program, Variable value, Variable count, BinaryInstruction operation) {}
    private static Fixture fixture(BinaryOperation operation, Integer constant, NumericOperandType type) {
        Program p = new Program(); BasicBlock block = p.createBasicBlock();
        Variable value = p.createVariable(), count = p.createVariable(), result = p.createVariable();
        if (constant != null) {
            IntegerConstantInstruction literal = new IntegerConstantInstruction();
            literal.setConstant(constant); literal.setReceiver(count); block.add(literal);
        }
        BinaryInstruction binary = new BinaryInstruction(operation,type);
        binary.setFirstOperand(value); binary.setSecondOperand(count); binary.setReceiver(result); block.add(binary);
        ExitInstruction exit = new ExitInstruction(); exit.setValueToReturn(result); block.add(exit);
        return new Fixture(p,value,count,binary);
    }
    private static Map<Variable,Instruction> definitions(Program program) {
        Map<Variable,Instruction> result = new IdentityHashMap<>();
        for (BasicBlock block : program.getBasicBlocks()) for (Instruction instruction : block) {
            if (instruction instanceof IntegerConstantInstruction c) result.put(c.getReceiver(),c);
            if (instruction instanceof BinaryInstruction b) result.put(b.getReceiver(),b);
        }
        return result;
    }
    public static void main(String[] args) {
        for (BinaryOperation operation : new BinaryOperation[]{BinaryOperation.SHIFT_LEFT,
                BinaryOperation.SHIFT_RIGHT,BinaryOperation.SHIFT_RIGHT_UNSIGNED}) {
            Fixture f = fixture(operation,null,NumericOperandType.INT);
            check(JavaShifts.lower(f.program)==1);
            Map<Variable,Instruction> defs = definitions(f.program);
            BinaryInstruction and = (BinaryInstruction)defs.get(f.operation.getSecondOperand());
            check(and.getOperation()==BinaryOperation.AND && and.getFirstOperand()==f.count
                && ((IntegerConstantInstruction)defs.get(and.getSecondOperand())).getConstant()==31);
            check(f.operation.getFirstOperand()==f.value);
        }
        for (int count : new int[]{0,31,32,-1,255}) {
            Fixture f = fixture(BinaryOperation.SHIFT_LEFT,count,NumericOperandType.INT);
            check(JavaShifts.lower(f.program)==(count>=0 && count<32 ? 0 : 1));
            check(((IntegerConstantInstruction)definitions(f.program).get(f.operation.getSecondOperand())).getConstant()==(count&31));
        }
        for (Integer divisor : new Integer[]{null,-1,Integer.MIN_VALUE,0,1,7}) {
            Fixture f = fixture(BinaryOperation.MODULO,divisor,NumericOperandType.INT);
            boolean lower = divisor==null || divisor<=0;
            check(JavaRemainders.lower(f.program)==(lower ? 1 : 0));
            Map<Variable,Instruction> defs = definitions(f.program);
            BinaryInstruction result = (BinaryInstruction)defs.get(f.operation.getReceiver());
            if (lower) {
                check(result.getOperation()==BinaryOperation.SUBTRACT && result.getFirstOperand()==f.value);
                BinaryInstruction multiply = (BinaryInstruction)defs.get(result.getSecondOperand());
                check(multiply.getOperation()==BinaryOperation.MULTIPLY && multiply.getSecondOperand()==f.count);
                BinaryInstruction divide = (BinaryInstruction)defs.get(multiply.getFirstOperand());
                check(divide.getOperation()==BinaryOperation.DIVIDE && divide.getFirstOperand()==f.value
                    && divide.getSecondOperand()==f.count);
            } else {
                check(result==f.operation);
            }
        }
        Fixture wide = fixture(BinaryOperation.SHIFT_LEFT,null,NumericOperandType.LONG);
        check(JavaShifts.lower(wide.program)==0);
        Fixture shared = fixture(BinaryOperation.SHIFT_LEFT,null,NumericOperandType.INT);
        BinaryInstruction sibling = new BinaryInstruction(BinaryOperation.SHIFT_RIGHT,NumericOperandType.INT);
        sibling.setFirstOperand(shared.value); sibling.setSecondOperand(shared.count);
        sibling.setReceiver(shared.program.createVariable()); shared.operation.insertNext(sibling);
        check(JavaShifts.lower(shared.program)==2);
        check(shared.operation.getSecondOperand()==sibling.getSecondOperand());
        Fixture branches = fixture(BinaryOperation.SHIFT_LEFT,null,NumericOperandType.INT);
        BasicBlock next = branches.program.createBasicBlock();
        BinaryInstruction other = new BinaryInstruction(BinaryOperation.SHIFT_RIGHT,NumericOperandType.INT);
        other.setFirstOperand(branches.value); other.setSecondOperand(branches.count);
        other.setReceiver(branches.program.createVariable()); next.add(other);
        check(JavaShifts.lower(branches.program)==2);
        check(branches.operation.getSecondOperand()!=other.getSecondOperand());
        System.out.println(cases);
    }
}
