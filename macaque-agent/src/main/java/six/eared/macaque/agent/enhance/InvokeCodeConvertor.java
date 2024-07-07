package six.eared.macaque.agent.enhance;

import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.classes.MethodDynamicStackVisitor;
import six.eared.macaque.asm.MethodVisitor;
import six.eared.macaque.asm.Opcodes;
import six.eared.macaque.common.util.ClassUtil;

import java.util.Map;

/**
 * 改变调用指令的字节码转换器
 */

public class InvokeCodeConvertor extends MethodDynamicStackVisitor {
    private final Map<String, MethodBindInfo> newMethods;

    public InvokeCodeConvertor(MethodVisitor write, Map<String, MethodBindInfo> bindMethods) {
        super(write);
        this.newMethods = bindMethods;
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs((maxStack*2) + 1, maxLocals);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        String uniqueDesc = name + "#" + desc;
        if (newMethods.containsKey(uniqueDesc)) {
            MethodBindInfo bindInfo = newMethods.get(uniqueDesc);
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
