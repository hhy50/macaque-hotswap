package six.eared.macaque.agent.enhance;


import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import six.eared.macaque.agent.accessor.Accessor;
import six.eared.macaque.agent.asm2.AsmUtil;
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
        accessor.accessField(this, opcode, owner, name, desc);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isInterface) {
        if (owner.contains("macaque$")) {
            super.visitMethodInsn(opcode, owner, name, desc, isInterface);
            return;
        }
        if (opcode != Opcodes.INVOKESTATIC) {
            // 非静态方法需要判断操作的变量是否是 slot[0]
            AbstractInsnNode insn = AsmUtil.getPrevStackInsn(Type.getArgumentTypes(desc).length, this.instructions.getLast());
            insn = AsmUtil.getPrev(insn);
            if (insn instanceof VarInsnNode
                    && insn.getOpcode() == Opcodes.ALOAD
                    && ((VarInsnNode) insn).var == 0) {
                accessor.accessMethod(this, owner, name, desc);
                return;
            }
        }
        super.visitMethodInsn(opcode, owner, name, desc, isInterface);
    }

    public void write(MethodVisitor writer) {
        AsmMethodVisitorCaller visitorCaller = this.method.getVisitorCaller();
        visitorCaller.accept(this);
        this.accept(writer);
    }
}
