package six.eared.macaque.agent.asm2.enhance;


import lombok.Getter;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.asm.ClassVisitor;
import six.eared.macaque.asm.MethodVisitor;
import six.eared.macaque.asm.Opcodes;

public class BindClassWriter extends ClassVisitor {

    private AsmMethod method;
    private final MethodBindInfo bindInfo;
    @Getter
    public byte[] bytecode;

    public BindClassWriter(AsmMethod method, MethodBindInfo bindInfo) {
        super(Opcodes.ASM5);
        this.method = method;
        this.bindInfo = bindInfo;
    }


    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (name.equals(bindInfo.getBindMethod())) {

        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    class MethodWriter extends MethodVisitor {
        public MethodWriter() {
            super(Opcodes.ASM5);
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            if (method.isStatic()) {
                maxStack += 1;
                maxLocals += 1;
            }
            super.visitMaxs(maxStack, maxLocals);
        }
    }
}
