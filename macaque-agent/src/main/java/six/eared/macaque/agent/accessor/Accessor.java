package six.eared.macaque.agent.accessor;

import lombok.Getter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.ClassFieldUniqueDesc;
import six.eared.macaque.agent.asm2.ClassMethodUniqueDesc;
import six.eared.macaque.agent.enhance.ClazzDataDefinition;
import six.eared.macaque.common.util.ClassUtil;

import java.util.Map;


public class Accessor {
    public static final String FIELD_GETTER_PREFIX = "$macaque$get$field$";
    public static final String FIELD_SETTER_PREFIX = "$macaque$set$field$";

    /**
     * 表示当前的访问器属于哪个类的
     */
    String ownerClass;
    Map<ClassMethodUniqueDesc, MethodAccessRule> methodAccessRules;
    Map<ClassFieldUniqueDesc, FieldAccessRule> fieldAccessRules;
    @Getter
    ClazzDataDefinition definition;
    Accessor parent;

    public String getClassName() {
        return definition.getClassName();
    }

    protected MethodAccessRule findMethodAccessRule(ClassMethodUniqueDesc uniqueDesc) {
        MethodAccessRule rule = methodAccessRules.get(uniqueDesc);
        if (rule != null) return rule;
        if (parent != null) return parent.findMethodAccessRule(uniqueDesc);
        return null;
    }

    protected FieldAccessRule findFieldAccessRule(ClassFieldUniqueDesc uniqueDesc) {
        FieldAccessRule rule = fieldAccessRules.get(uniqueDesc);
        if (rule != null) return rule;
        if (parent != null) return parent.findFieldAccessRule(uniqueDesc);
        return null;
    }

    public void accessMethod(InsnList insnList, int opcode, String owner, String name, String desc) {
        if (opcode != Opcodes.INVOKESTATIC) {
            // 非静态需要判断操作的变量是否是 slot[0]
            AbstractInsnNode insn = AsmUtil.getPrevStackInsn(Type.getArgumentTypes(desc).length, AsmUtil.getPrevValid(insnList.getLast()));
            if (!isAload0(insn)) {
                insnList.add(new MethodInsnNode(opcode, owner, name, desc, false));
                return;
            }
        }
        MethodAccessRule accessRule = findMethodAccessRule(ClassMethodUniqueDesc.of(ClassUtil.classpath2name(owner), name, desc));
        if (accessRule == null) {
            accessRule = MethodAccessRule.direct();
        }
        accessRule.access(insnList, opcode, owner, name, desc, false);
    }

    public void accessField(InsnList insnList, int opcode, String owner, String name, String type) {
        if (opcode != Opcodes.GETSTATIC && opcode != Opcodes.PUTSTATIC) {
            // 非静态需要判断操作的变量是否是 slot[0]
            AbstractInsnNode insn = AsmUtil.getPrevStackInsn(opcode==Opcodes.PUTFIELD?1:0, AsmUtil.getPrevValid(insnList.getLast()));
            if (!isAload0(insn)) {
                insnList.add(new FieldInsnNode(opcode, owner, name, type));
                return;
            }
        }

        FieldAccessRule accessRule = findFieldAccessRule(ClassFieldUniqueDesc.of(ClassUtil.classpath2name(owner), name, type));
        if (accessRule == null) {
            accessRule = FieldAccessRule.direct();
        }
        accessRule.access(insnList, opcode, owner, name, type);
    }

    private boolean isAload0(AbstractInsnNode insn) {
        return insn instanceof VarInsnNode
                && insn.getOpcode() == Opcodes.ALOAD
                && ((VarInsnNode) insn).var == 0;
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
