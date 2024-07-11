package six.eared.macaque.agent.enhance;

import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.classes.MethodDynamicStackVisitor;
import six.eared.macaque.asm.MethodVisitor;
import six.eared.macaque.asm.Opcodes;
import six.eared.macaque.common.util.ClassUtil;

/**
 * 改变调用指令的字节码转换器
 */

public class InvokeCodeConvertor extends MethodDynamicStackVisitor {

    public InvokeCodeConvertor(AsmMethod method, MethodVisitor write) {
        super(write);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        // 加1 是需要访问器入栈
        super.visitMaxs(maxStack+1, maxLocals);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        MethodBindInfo bindInfo = MethodBindManager.getBindInfo(ClassUtil.classpath2name(owner), name,
                desc, opcode == Opcodes.INVOKESTATIC);
        if (bindInfo != null) {
            String accessorClassPath = ClassUtil.simpleClassName2path(bindInfo.getAccessorClass());
            if (opcode != Opcodes.INVOKESTATIC) {
                // 需要先将this弹出
                super.visitVarInsn(Opcodes.ASTORE, 0);

                // TODO 需要访问器提前入栈
                AsmUtil.accessorStore(this, accessorClassPath);
            } else {
                // 只需要访问器入栈
                AsmUtil.accessorStore(this, accessorClassPath);
            }
            super.visitMethodInsn(Opcodes.INVOKESTATIC, ClassUtil.simpleClassName2path(bindInfo.getBindClass()), bindInfo.getBindMethod(),
                    bindInfo.getBindMethodDesc(), itf);
            return;
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }
}
