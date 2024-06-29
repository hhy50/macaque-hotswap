package six.eared.macaque.agent.asm2.classes;

import six.eared.macaque.asm.MethodVisitor;
import six.eared.macaque.common.util.Pair;

import java.util.Stack;

import static six.eared.macaque.asm.Opcodes.ASM5;

public class MethodDynamicStackVisitor extends MethodVisitor {

    /**
     * 操作码、操作数
     */
    private final Stack opStack = new Stack<Pair<Integer /* opcode */, Integer /* opc */>>();

    public MethodDynamicStackVisitor(MethodVisitor write) {
        super(ASM5, write);
    }
}
