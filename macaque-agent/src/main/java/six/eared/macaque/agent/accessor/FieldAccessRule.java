package six.eared.macaque.agent.accessor;

import org.objectweb.asm.tree.InsnList;
import six.eared.macaque.common.util.ClassUtil;

public interface FieldAccessRule {

    /**
     * 访问
     *
     * @param insnList
     * @param opcode
     * @param owner
     * @param name
     */
    void access(InsnList insnList, int opcode, String owner, String name, String type);

    /**
     * 对字段的访问转移到具体方法上
     * @param isStatic
     * @param targetClass
     * @param getter
     * @param setter
     * @return
     */
    static FieldAccessRule forwardToMethod(boolean isStatic, String targetClass, String getter, String setter) {
        return new FieldForwardMethodAccess(isStatic, ClassUtil.className2path(targetClass), getter, setter);
    }

    static FieldAccessRule direct() {
        return FieldDirectAccess.INSTANCE;
    }
}
