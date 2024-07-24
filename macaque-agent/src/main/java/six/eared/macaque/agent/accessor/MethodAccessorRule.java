package six.eared.macaque.agent.accessor;

import org.objectweb.asm.tree.InsnList;
import six.eared.macaque.common.util.ClassUtil;

public interface MethodAccessorRule {

    /**
     * 访问
     *
     * @param insnList
     * @param opcode
     * @param owner
     * @param name
     * @param desc
     * @param isInterface
     */
    void access(InsnList insnList, int opcode, String owner, String name, String desc, boolean isInterface);

    /**
     * 将方法的访问规则转发到目标方法
     *
     * @param targetOwner
     * @param targetMethodName
     * @param targetDesc
     * @return
     */
    static MethodAccessorRule forward(boolean isStatic, String targetOwner, String targetMethodName, String targetDesc) {
        return new MethodForwardAccess(isStatic, ClassUtil.className2path(targetOwner), targetMethodName, targetDesc);
    }

    static MethodAccessorRule direct() {
        return MethodDirectAccess.INSTANCE;
    }
}
