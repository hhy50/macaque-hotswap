package six.eared.macaque.agent.enhance;


import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;
import six.eared.macaque.agent.accessor.Accessor;


public class BindMethodWriter extends MethodNode {

    private Accessor accessor;

    public BindMethodWriter(Accessor accessor) {
        super(Opcodes.ASM9);
        this.accessor = accessor;
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {

    }

    @Override
    public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {

    }

    @Override
    public void visitLineNumber(int line, Label start) {

    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {

    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        if (owner.contains("$macaque$")) {
            super.visitFieldInsn(opcode, owner, name, desc);
            return;
        }
        accessor.accessField(this.instructions, opcode, owner, name, desc);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isInterface) {
        if (owner.contains("macaque$")) {
            super.visitMethodInsn(opcode, owner, name, desc, isInterface);
            return;
        }
        accessor.accessArgs(this.instructions, opcode, owner, name, desc, isInterface);
        accessor.accessMethod(this.instructions, opcode, owner, name, desc, isInterface);
    }
}
