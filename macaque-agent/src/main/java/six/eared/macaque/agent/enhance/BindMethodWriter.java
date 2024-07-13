package six.eared.macaque.agent.enhance;


import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import six.eared.macaque.agent.asm2.AsmMethod;

public class BindMethodWriter extends MethodVisitor {

    private AsmMethod method;

    private MethodBindInfo bindInfo;

    public BindMethodWriter(MethodVisitor writer, AsmMethod method, MethodBindInfo bindInfo) {
        super(Opcodes.ASM9, writer);
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
