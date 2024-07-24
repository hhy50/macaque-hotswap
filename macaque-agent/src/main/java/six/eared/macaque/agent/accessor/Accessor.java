package six.eared.macaque.agent.accessor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import six.eared.macaque.agent.asm2.AsmUtil;
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

    Accessor parent;

    public String getClassName() {
        return accessor.getClassName();
    }

    public void accessMethod(InsnList insnList, int opcode, String owner, String name, String desc) {
        if (opcode != Opcodes.INVOKESTATIC) {
            // 非静态方法需要判断操作的变量是否是 slot[0]
            AbstractInsnNode insn = AsmUtil.getPrevStackInsn(Type.getArgumentTypes(desc).length, insnList.getLast());
            insn = AsmUtil.getPrev(insn);
            if (insn instanceof VarInsnNode
                    && insn.getOpcode() == Opcodes.ALOAD
                    && ((VarInsnNode) insn).var == 0) {
                String accessorClass = findAccessorClass(owner);
                insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, accessorClass, name, desc, false));
                return;
            }
        } else {
            String accessorClass = findAccessorClass(owner);
            if (accessorClass != null) {
                insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, accessorClass, name, desc, false));
                return;
            }
        }
        insnList.add(new MethodInsnNode(opcode, owner, name, desc, false));
    }

    public void accessField(MethodVisitor visitor, int opcode, String owner, String name, String desc) {
        String accessorClass = findAccessorClass(owner);
        if (accessorClass == null) {
            visitor.visitFieldInsn(opcode, owner, name, desc);
            return;
        }
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
