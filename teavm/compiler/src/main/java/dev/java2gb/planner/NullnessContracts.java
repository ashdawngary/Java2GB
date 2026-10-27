package dev.java2gb.planner;

import java.util.*;
import org.teavm.model.*;
import org.teavm.model.instructions.*;

final class NullnessContracts implements ClassHolderTransformer {
    private final String prefix;
    private int lowered;
    NullnessContracts(String prefix) { this.prefix = prefix; }
    @Override public void transformClass(ClassHolder cls, ClassHolderTransformerContext context) {
        if (!ApplicationScope.contains(prefix, cls.getName())) return;
        for (MethodHolder method : cls.getMethods()) {
            Program program = method.getProgram();
            if (program == null) continue;
            for (BasicBlock block : program.getBasicBlocks()) {
                List<Instruction> instructions = new ArrayList<>();
                for (Instruction insn : block) instructions.add(insn);
                for (Instruction insn : instructions) {
                    if (!(insn instanceof InvokeInstruction call)
                            || !call.getMethod().getClassName().equals("java.util.Objects")
                            || !call.getMethod().getDescriptor().toString().equals("requireNonNull(Ljava/lang/Object;)Ljava/lang/Object;")) continue;
                    NullCheckInstruction check = new NullCheckInstruction();
                    check.setValue(call.getArguments().get(0));
                    check.setReceiver(call.getReceiver() != null ? call.getReceiver() : program.createVariable());
                    check.setLocation(call.getLocation());
                    call.replace(check);
                    lowered++;
                }
            }
        }
    }
    int lowered() { return lowered; }
}
