package six.eared.macaque.agent.asm2.classes;

import six.eared.macaque.agent.asm2.AsmField;
import six.eared.macaque.asm.FieldVisitor;

import static six.eared.macaque.asm.Opcodes.ASM4;

public class AsmFieldReader extends FieldVisitor {

    private final AsmField asmField;

    public AsmFieldReader(AsmField asmField) {
        super(ASM4);
        this.asmField = asmField;
    }
}
