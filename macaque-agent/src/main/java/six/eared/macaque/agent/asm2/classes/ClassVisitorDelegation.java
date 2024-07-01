package six.eared.macaque.agent.asm2.classes;

import six.eared.macaque.asm.ClassVisitor;
import six.eared.macaque.asm.Opcodes;

public class ClassVisitorDelegation extends ClassVisitor {
    public ClassVisitorDelegation(ClassVisitor delegation) {
        super(Opcodes.ASM5, delegation);
    }
}
