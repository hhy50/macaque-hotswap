package six.eared.macaque.agent.asm2.classes;


import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;


public class MethodDynamicStackVisitor extends MethodNode {

    protected MethodDynamicStackVisitor() {
        super(Opcodes.ASM9);
    }
}
