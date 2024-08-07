package six.eared.macaque.agent.enhance;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;
import six.eared.macaque.agent.accessor.MethodAccessRule;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.common.util.ClassUtil;

/**
 * 改变调用指令的字节码转换器
 */

public class InvokeCodeConvertor extends MethodNode {

    private final AsmMethod method;
    private final MethodVisitor write;
    private int maxArglen = 0;
    private boolean accessorLoad = false;

    public InvokeCodeConvertor(AsmMethod method, MethodVisitor write) {
        super(Opcodes.ASM9);
        this.method = method;
        this.write = write;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        MethodBindInfo bindInfo = MethodBindManager.getBindInfo(ClassUtil.classpath2name(owner), name,
                desc, opcode == Opcodes.INVOKESTATIC);
        if (bindInfo != null) {
            this.maxArglen = Math.max(Type.getArgumentTypes(desc).length, maxArglen);
            this.accessorLoad = opcode != Opcodes.INVOKESTATIC;
            MethodAccessRule accessRule = bindInfo.getAccessRule(false);
            accessRule.access(this.instructions, opcode, owner, name, desc, itf);
            return;
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    @Override
    public void visitEnd() {
        int minStack = maxArglen + (accessorLoad ? 3 : 0);
        this.maxStack = Math.max(minStack, maxStack);
        super.visitEnd();
        this.accept(write);
    }
}
