package six.eared.macaque.agent.asm2.enhance;


import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.asm.MethodVisitor;
import six.eared.macaque.asm.Opcodes;

public class MethodByteCodeConvertor extends MethodVisitor {

    private final AsmMethod asmMethod;

    public MethodByteCodeConvertor(AsmMethod asmMethod) {
        super(Opcodes.ASM5);
        this.asmMethod = asmMethod;
    }
}
