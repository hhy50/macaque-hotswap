package six.eared.macaque.agent.asm2.classes;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

public class AsmMethodVisitorCaller extends MethodNode {

    public AsmMethodVisitorCaller() {
        super(Opcodes.ASM9);
    }

    public void accept(MethodVisitor mv) {
        if (!this.isEmpty())
            super.accept(mv);
    }

    public boolean isEmpty() {
        return this.instructions == null
                || this.instructions.size() == 0;
    }
}
