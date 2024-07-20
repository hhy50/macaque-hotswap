package six.eared.macaque.agent.accessor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.common.util.ClassUtil;


@AllArgsConstructor
public class Accessor {
    public static final String FIELD_GETTER_PREFIX = "macaque$get$field$";
    public static final String FIELD_SETTER_PREFIX = "macaque$set$field$";

    /**
     * 表示当前的访问器属于哪个类的
     */
    @Getter
    private String ownerClass;

    @Getter
    private ClazzDefinition accessor;

    @Getter
    private Accessor parent;

    public String getClassName() {
        return accessor.getClassName();
    }

    public void accessMethod(MethodVisitor visitor, String owner, String name, String desc) {
        String accessorClass = findAccessorClass(owner);
        visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, accessorClass, name, desc, false);
    }

    public void accessField(MethodVisitor visitor, int opcode, String owner, String name, String desc) {
        String accessorClass = findAccessorClass(owner);
        if (opcode == Opcodes.GETSTATIC) {
            visitor.visitMethodInsn(Opcodes.INVOKESTATIC, accessorClass, Accessor.FIELD_GETTER_PREFIX + name, "(" + ")" + desc, false);
        } else if (opcode == Opcodes.GETFIELD) {
            // 这里本来就是对this/super (ALOAD_0) 访问的，不要将访问器入栈
            visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, accessorClass, Accessor.FIELD_GETTER_PREFIX + name, "(" + ")" + desc, false);
        }

        if (opcode == Opcodes.PUTSTATIC) {
            visitor.visitMethodInsn(Opcodes.INVOKESTATIC, accessorClass, Accessor.FIELD_SETTER_PREFIX + name, "(" + desc + ")V", false);
        } else if (opcode == Opcodes.PUTFIELD) {
            visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, accessorClass, Accessor.FIELD_SETTER_PREFIX + name, "(" + desc + ")V", false);
        }
    }

    private String findAccessorClass(String owner) {
        String accessorOwner = ClassUtil.simpleClassName2path(this.ownerClass);
        if (accessorOwner.equals(owner)) {
            return ClassUtil.simpleClassName2path(getClassName());
        }
        if (parent == null) {
            return null;
        }
        return parent.findAccessorClass(owner);
    }
}
