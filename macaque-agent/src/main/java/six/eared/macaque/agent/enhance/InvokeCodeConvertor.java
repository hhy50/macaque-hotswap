package six.eared.macaque.agent.enhance;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.AsmUtil;
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
            String accessorClassPath = ClassUtil.className2path(bindInfo.getAccessorClass());
            if (opcode == Opcodes.INVOKESTATIC) {
                // 只需要访问器入栈
                //AsmUtil.accessorStore(this.instructions, accessorClassPath);
            } else {
                this.accessorLoad = true;
                /**
                 * 需要访问器提前入栈
                 * 先找到压入参数之前的第一条指令
                 */
                AbstractInsnNode prev = AsmUtil.getPrevStackInsn(Type.getArgumentTypes(desc).length,AsmUtil.getPrevValid(this.instructions.getLast()));
                InsnList inst = new InsnList();
                // 需要先将obj弹出
                inst.add(new InsnNode(Opcodes.POP));
                // 访问器入栈
                AsmUtil.accessorStore(inst, accessorClassPath);
                this.instructions.insert(prev, inst);
            }
            super.visitMethodInsn(Opcodes.INVOKESTATIC, ClassUtil.className2path(bindInfo.getBindClass()), bindInfo.getBindMethod(),
                    bindInfo.getBindMethodDesc(), itf);
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
