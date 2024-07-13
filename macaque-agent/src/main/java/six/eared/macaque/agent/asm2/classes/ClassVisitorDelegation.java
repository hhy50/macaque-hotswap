package six.eared.macaque.agent.asm2.classes;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

public class ClassVisitorDelegation extends ClassVisitor {
    public ClassVisitorDelegation(ClassVisitor delegation) {
        super(Opcodes.ASM9, delegation);
    }
}
