package six.eared.macaque.agent.enhance;


import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.asm.MethodVisitor;
import six.eared.macaque.asm.Opcodes;

public class BindMethodWriter extends MethodVisitor {

    private AsmMethod method;

    private MethodBindInfo bindInfo;

    public BindMethodWriter(MethodVisitor writer, AsmMethod method, MethodBindInfo bindInfo) {
        super(Opcodes.ASM5, writer);
        this.method = method;
        this.bindInfo = bindInfo;
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        if (method.isStatic()) {
            maxLocals += 1;
        }
        super.visitMaxs(maxStack, maxLocals);
    }
}
