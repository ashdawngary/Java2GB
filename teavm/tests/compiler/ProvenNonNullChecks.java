package dev.java2gb.planner;

import java.util.*;
import org.teavm.model.*;
import org.teavm.model.instructions.*;

public final class ProvenNonNullChecks {
    static int cases;
    static void check(boolean value) { cases++; if (!value) throw new AssertionError("Nullness case " + cases); }
    static ConstructInstruction make(Variable value) {
        ConstructInstruction make=new ConstructInstruction();make.setType("probe.Box");make.setReceiver(value);return make;
    }
    static NullCheckInstruction guard(Variable value,Program p) {
        NullCheckInstruction guard=new NullCheckInstruction();guard.setValue(value);guard.setReceiver(p.createVariable());return guard;
    }
    static int count(Program p) {
        int n=0;for(BasicBlock block:p.getBasicBlocks())for(Instruction insn:block)if(insn instanceof NullCheckInstruction)n++;return n;
    }
    static void fieldCase(int mode,boolean eliminated) {
        MutableClassHolderSource classes=new MutableClassHolderSource();
        ClassHolder cls=new ClassHolder("probe.Owner");classes.putClassHolder(cls);
        FieldHolder field=new FieldHolder("value");field.setType(ValueType.object("probe.Box"));field.getModifiers().add(ElementModifier.STATIC);
        if(mode!=1)field.getModifiers().add(ElementModifier.FINAL);cls.addField(field);
        Program init=new Program();BasicBlock block=init.createBasicBlock();Variable value=init.createVariable();
        if(mode==2) {NullConstantInstruction nil=new NullConstantInstruction();nil.setReceiver(value);block.add(nil);}
        else if(mode==6 || mode==7) {
            InvokeInstruction call=new InvokeInstruction();call.setType(InvocationType.SPECIAL);
            call.setMethod(new MethodReference("probe.Factory","value",ValueType.object("probe.Box")));call.setReceiver(value);block.add(call);
        } else block.add(make(value));
        PutFieldInstruction put=new PutFieldInstruction();put.setField(field.getReference());put.setFieldType(field.getType());put.setValue(value);block.add(put);
        if(mode==3) {PutFieldInstruction again=new PutFieldInstruction();again.setField(field.getReference());again.setFieldType(field.getType());again.setValue(value);block.add(again);}
        Program read=new Program();BasicBlock use=read.createBasicBlock();Variable loaded=read.createVariable();
        GetFieldInstruction get=new GetFieldInstruction();get.setField(field.getReference());get.setFieldType(field.getType());get.setReceiver(loaded);use.add(get);use.add(guard(loaded,read));
        MethodReference initRef=new MethodReference("probe.Owner","<clinit>",ValueType.VOID);
        MethodReference readRef=new MethodReference("probe.Reader","read",ValueType.VOID);
        Map<MethodReference,Program> programs=new LinkedHashMap<>();programs.put(initRef,init);programs.put(readRef,read);
        if(mode==4 || mode==5) {
            InvokeInstruction early=new InvokeInstruction();early.setType(InvocationType.SPECIAL);early.setMethod(readRef);
            if(mode==4)put.insertPrevious(early);
            else {Program other=new Program();other.createBasicBlock().add(early);programs.put(new MethodReference("probe.Other","<clinit>",ValueType.VOID),other);}
        }
        ProvenNonNull proof=new ProvenNonNull(classes,programs,ref->mode==7 && ref.getClassName().equals("probe.Factory"));
        check(count(read)==(eliminated?0:1));check(proof.at(readRef,loaded)==eliminated);
    }
    public static void main(String[] args) {
        for(int mode=0;mode<8;mode++)fieldCase(mode,mode==0 || mode==7);
        Program p=new Program();BasicBlock block=p.createBasicBlock();Variable parameter=p.createVariable();
        NullCheckInstruction first=guard(parameter,p);block.add(first);block.add(guard(first.getReceiver(),p));
        new ProvenNonNull(new MutableClassHolderSource(),Map.of(new MethodReference("probe.Test","run",ValueType.VOID),p),ref->false);
        check(count(p)==1);  
        Program local=new Program();BasicBlock body=local.createBasicBlock();Variable object=local.createVariable();body.add(make(object));body.add(guard(object,local));
        new ProvenNonNull(new MutableClassHolderSource(),Map.of(new MethodReference("probe.Test","allocated",ValueType.VOID),local),ref->false);
        check(count(local)==0);
        System.out.println(cases);
    }
}
