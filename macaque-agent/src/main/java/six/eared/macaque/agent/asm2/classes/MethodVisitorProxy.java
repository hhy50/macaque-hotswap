package six.eared.macaque.agent.asm2.classes;


import six.eared.macaque.asm.IMethodVisitor;
import six.eared.macaque.asm.MethodVisitor;
import six.eared.macaque.asm.Opcodes;

public class MethodVisitorProxy extends MethodVisitor {

    public MethodVisitorProxy(IMethodVisitor mv) {
        super(Opcodes.ASM5, mv);
    }
}
