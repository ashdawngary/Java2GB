package dev.java2gb.planner;

import org.teavm.model.*;
import org.teavm.model.instructions.*;

public final class CheckedRomReadsChecks {
    private static int cases;
    private static void check(boolean value) {
        cases++;
        if (!value) throw new AssertionError("Checked ROM read case " + cases);
    }
    private static Program fixture(int kind) {
        Program p = new Program();
        Variable array=p.createVariable(), index=p.createVariable(), checked=p.createVariable();
        Variable unwrapped=p.createVariable(), value=p.createVariable(), other=p.createVariable();
        BasicBlock block=p.createBasicBlock();
        BoundCheckInstruction bound=new BoundCheckInstruction();
        bound.setArray(array); bound.setIndex(index); bound.setReceiver(checked); block.add(bound);
        if (kind==1) {
            InvokeInstruction effect=new InvokeInstruction();effect.setType(InvocationType.SPECIAL);
            effect.setMethod(new MethodReference("probe.Effect","run",ValueType.VOID));block.add(effect);
        }
        if (kind==2) {
            NullCheckInstruction nullCheck=new NullCheckInstruction();
            nullCheck.setValue(other);nullCheck.setReceiver(p.createVariable());block.add(nullCheck);
        }
        UnwrapArrayInstruction unwrap=new UnwrapArrayInstruction(ArrayElementType.BYTE);
        unwrap.setArray(kind==3 ? other : array);unwrap.setReceiver(unwrapped);block.add(unwrap);
        GetElementInstruction get=new GetElementInstruction(ArrayElementType.BYTE);
        get.setArray(unwrapped);get.setIndex(checked);get.setReceiver(value);block.add(get);
        if (kind==4) {
            AssignInstruction use=new AssignInstruction();use.setAssignee(checked);use.setReceiver(p.createVariable());block.add(use);
        }
        if (kind==5) {
            BasicBlock next=p.createBasicBlock();
            unwrap.delete();get.delete();
            JumpInstruction jump=new JumpInstruction();jump.setTarget(next);block.add(jump);
            next.add(unwrap);next.add(get);block=next;
        }
        ExitInstruction exit=new ExitInstruction();exit.setValueToReturn(value);block.add(exit);
        return p;
    }
    public static void main(String[] args) {
        Program safe=fixture(0);
        check(CheckedRomReads.optimize(safe,array -> true)==1);
        check(CheckedRomReads.optimize(safe,array -> true)==0);
        for (int kind=1;kind<=5;kind++) check(CheckedRomReads.optimize(fixture(kind),array -> true)==0);
        check(CheckedRomReads.optimize(fixture(0),array -> false)==0);
        System.out.println(cases);
    }
}
