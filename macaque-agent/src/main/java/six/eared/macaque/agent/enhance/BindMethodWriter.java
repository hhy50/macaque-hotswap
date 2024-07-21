package six.eared.macaque.agent.enhance;


import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;
import six.eared.macaque.agent.accessor.Accessor;
import six.eared.macaque.agent.asm2.classes.AsmMethodVisitorCaller;


public class BindMethodWriter extends MethodNode {

    private MethodUpdateInfo method;

    private Accessor accessor;

    public BindMethodWriter(MethodUpdateInfo method, Accessor accessor) {
        super(Opcodes.ASM9);
        this.method = method;
        this.accessor = accessor;
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        if (owner.contains("macaque$")) {
            super.visitFieldInsn(opcode, owner, name, desc);
            return;
        }
        accessor.accessField(this, opcode, owner, name, desc);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isInterface) {
        if (owner.contains("macaque$")) {
            super.visitMethodInsn(opcode, owner, name, desc, isInterface);
            return;
        }
        accessor.accessMethod(this.instructions, opcode, owner, name, desc);
    }

    public void write(MethodVisitor writer) {
        AsmMethodVisitorCaller visitorCaller = this.method.getVisitorCaller();
        visitorCaller.accept(this);
        this.accept(writer);
    }
}
