package six.eared.macaque.agent.accessor;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.ClassMethodUniqueDesc;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.common.util.ClassUtil;

import java.util.Map;


public class Accessor {
    public static final String FIELD_GETTER_PREFIX = "$macaque$get$field$";
    public static final String FIELD_SETTER_PREFIX = "$macaque$set$field$";

    /**
     * 表示当前的访问器属于哪个类的
     */
    String ownerClass;

    Map<ClassMethodUniqueDesc, MethodAccessorRule> methodAccessorRules;

    ClazzDefinition definition;

    Accessor parent;

    public String getClassName() {
        return definition.getClassName();
    }

    protected MethodAccessorRule findMethodAccessRule(String owner, String name, String desc) {
        MethodAccessorRule rule = methodAccessorRules.get(ClassMethodUniqueDesc.of(ClassUtil.classpath2name(owner), name, desc));
        if (rule != null) return rule;
        if (parent != null) return parent.findMethodAccessRule(owner, name, desc);
        return MethodAccessorRule.direct();
    }

    public void accessMethod(InsnList insnList, int opcode, String owner, String name, String desc) {
        if (opcode != Opcodes.INVOKESTATIC) {
            // 非静态方法需要判断操作的变量是否是 slot[0]
            AbstractInsnNode insn = AsmUtil.getPrevStackInsn(Type.getArgumentTypes(desc).length, insnList.getLast());
            insn = AsmUtil.getPrev(insn);
            if (insn instanceof VarInsnNode
                    && insn.getOpcode() == Opcodes.ALOAD
                    && ((VarInsnNode) insn).var == 0) {
                MethodAccessorRule methodAccessRule = findMethodAccessRule(owner, name, desc);
                methodAccessRule.access(insnList, opcode, owner, name, desc, false);
                return;
            }
            insnList.add(new MethodInsnNode(opcode, owner, name, desc, false));
        } else {
            MethodAccessorRule methodAccessRule = findMethodAccessRule(owner, name, desc);
            methodAccessRule.access(insnList, opcode, owner, name, desc, false);
        }
    }

    public void accessField(MethodVisitor visitor, int opcode, String owner, String name, String desc) {
        String accessorClass = findAccessorClass(owner);
        if (accessorClass == null) {
            visitor.visitFieldInsn(opcode, owner, name, desc);
            return;
        }
        if (opcode == Opcodes.GETSTATIC) {
            visitor.visitMethodInsn(Opcodes.INVOKESTATIC, accessorClass, Accessor.FIELD_GETTER_PREFIX+name, "("+")"+desc, false);
        } else if (opcode == Opcodes.GETFIELD) {
            // 这里本来就是对this/super (ALOAD_0) 访问的，不要将访问器入栈
            visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, accessorClass, Accessor.FIELD_GETTER_PREFIX+name, "("+")"+desc, false);
        }

        if (opcode == Opcodes.PUTSTATIC) {
            visitor.visitMethodInsn(Opcodes.INVOKESTATIC, accessorClass, Accessor.FIELD_SETTER_PREFIX+name, "("+desc+")V", false);
        } else if (opcode == Opcodes.PUTFIELD) {
            visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, accessorClass, Accessor.FIELD_SETTER_PREFIX+name, "("+desc+")V", false);
        }
    }

    private String findAccessorClass(String owner) {
        String accessorOwner = ClassUtil.className2path(this.ownerClass);
        if (accessorOwner.equals(owner)) {
            return ClassUtil.className2path(getClassName());
        }
        if (parent == null) {
            return null;
        }
        return parent.findAccessorClass(owner);
    }
}
