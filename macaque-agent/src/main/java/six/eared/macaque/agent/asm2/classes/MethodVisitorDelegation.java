package six.eared.macaque.agent.asm2.classes;


import six.eared.macaque.asm.IMethodVisitor;
import six.eared.macaque.asm.MethodVisitor;
import six.eared.macaque.asm.Opcodes;

public class MethodVisitorDelegation extends MethodVisitor {

    public MethodVisitorDelegation(IMethodVisitor mv) {
        super(Opcodes.ASM5, mv);
    }
}
