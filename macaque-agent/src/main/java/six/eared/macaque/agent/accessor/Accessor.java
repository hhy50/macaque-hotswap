package six.eared.macaque.agent.accessor;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import six.eared.macaque.agent.enhance.MethodBindInfo;
import six.eared.macaque.common.util.ClassUtil;

public class Accessor {
    public static final String FIELD_GETTER_PREFIX = "macaque$get$field$";
    public static final String FIELD_SETTER_PREFIX = "macaque$set$field$";


    public static void accessField(MethodBindInfo bindInfo, MethodVisitor writer, int opcode, String owner, String name, String desc) {
        if (opcode == Opcodes.GETSTATIC || opcode == Opcodes.GETFIELD) {
            String accessorClass = bindInfo.getAccessorClass();
            writer.visitMethodInsn(Opcodes.INVOKESTATIC, ClassUtil.className2path(accessorClass), FIELD_GETTER_PREFIX + name, "(" + ")L" + desc + ";", false);
        } else {

        }
    }
}
