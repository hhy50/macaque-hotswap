package six.eared.macaque.agent.asm2.classes;

import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.asm.MethodVisitor;

import static six.eared.macaque.asm.Opcodes.ASM4;

public class AsmMethodReader extends MethodVisitor {

    private final AsmMethod asmMethod;

    public AsmMethodReader(AsmMethod asmMethod) {
        super(ASM4);
        this.asmMethod = asmMethod;
    }
}
