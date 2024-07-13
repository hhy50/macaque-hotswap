package six.eared.macaque.agent.asm2.classes;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import six.eared.macaque.common.util.Pair;

import java.util.Stack;


public class MethodDynamicStackVisitor extends MethodVisitor {

    /**
     * 操作码、操作数
     */
    private final Stack<Pair<Integer /* opcode */, Object /* opc */>> opStack = new Stack<>();

    public MethodDynamicStackVisitor(MethodVisitor write) {
        super(Opcodes.ASM9, write);
    }

    public String a() {
        throw new NoSuchMethodError();
    }

    public static String b() {
        throw new NoSuchMethodError();
    }
}
