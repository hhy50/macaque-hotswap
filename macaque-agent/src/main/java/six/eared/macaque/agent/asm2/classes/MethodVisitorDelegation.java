package six.eared.macaque.agent.asm2.classes;


import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MethodVisitorDelegation extends MethodVisitor {

    public MethodVisitorDelegation(MethodVisitor mv) {
        super(Opcodes.ASM9, mv);
    }
}
