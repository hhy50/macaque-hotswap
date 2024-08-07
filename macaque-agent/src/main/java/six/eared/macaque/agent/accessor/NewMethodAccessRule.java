package six.eared.macaque.agent.accessor;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.enhance.MethodBindInfo;
import six.eared.macaque.common.util.ClassUtil;

public class NewMethodAccessRule implements MethodAccessRule {

    private final MethodBindInfo bindInfo;

    /**
     * 是否是bindMethod访问?
     */
    private final boolean bindAccess;

    public NewMethodAccessRule(MethodBindInfo bindInfo, boolean bindAccess) {
        this.bindInfo = bindInfo;
        this.bindAccess = bindAccess;
    }

    @Override
    public void access(InsnList insnList, int opcode, String owner, String name, String desc, boolean isInterface) {
        String accessorClassPath = ClassUtil.className2path(bindInfo.getAccessorClass());
        if (opcode != Opcodes.INVOKESTATIC) {
            /**
             * 需要访问器提前入栈
             * 先找到压入参数之前的第一条指令
             */
            AbstractInsnNode prev = AsmUtil.getPrevStackInsn(Type.getArgumentTypes(desc).length, AsmUtil.getPrevValid(insnList.getLast()));
            InsnList inst = new InsnList();
            inst.add(new InsnNode(Opcodes.POP));  // 需要先将this弹出, 然后把对this的访问替换成accessor
            if (bindAccess) {
                inst.add(new VarInsnNode(Opcodes.ALOAD, 0));
            } else {
                AsmUtil.accessorStore(inst, accessorClassPath); // 访问器入栈
            }
            insnList.insert(prev, inst);
        }
        insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ClassUtil.className2path(bindInfo.getBindClass()), bindInfo.getBindMethod(),
                bindInfo.getBindMethodDesc(), isInterface));
    }
}
