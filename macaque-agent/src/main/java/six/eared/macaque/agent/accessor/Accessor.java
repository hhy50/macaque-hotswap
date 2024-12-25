package six.eared.macaque.agent.accessor;

import lombok.Getter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import six.eared.macaque.agent.asm2.ClassFieldUniqueDesc;
import six.eared.macaque.agent.asm2.ClassMethodUniqueDesc;
import six.eared.macaque.agent.enhance.ClazzDataDefinition;
import six.eared.macaque.agent.enhance.MethodBindInfo;
import six.eared.macaque.agent.enhance.MethodBindManager;
import six.eared.macaque.common.util.ClassUtil;

import java.util.Map;

import static six.eared.macaque.agent.accessor.AccessorClassBuilder.GET_ORIGIN_MNAME;


public class Accessor {
    public static final String FIELD_GETTER_PREFIX = "macaque$get$field$";
    public static final String FIELD_SETTER_PREFIX = "macaque$set$field$";
    public String this$0;

    /**
     * 表示当前的访问器属于哪个类的
     */
    Map<ClassMethodUniqueDesc, MethodAccessRule> methodAccessRules;
    Map<ClassFieldUniqueDesc, FieldAccessRule> fieldAccessRules;
    @Getter
    ClazzDataDefinition definition;
    Accessor parent;

    public String getClassName() {
        return definition.getClassName();
    }

    public void accessSelf(InsnList instructions, AbstractInsnNode load0) {
        instructions.insert(load0,
                new MethodInsnNode(Opcodes.INVOKEVIRTUAL, ClassUtil.className2path(definition.getClassName()),
                        GET_ORIGIN_MNAME, "()L"+ClassUtil.className2path(this$0)+";")
        );
    }

    public void accessMethod(InsnList insnList, int opcode, String owner, String name, String desc, boolean isInterface) {
        MethodAccessRule accessRule = findMethodAccessRule(ClassMethodUniqueDesc.of(ClassUtil.classpath2name(owner), name, desc));
        if (accessRule == null && opcode == Opcodes.INVOKEVIRTUAL) {
            accessRule = findMethodVirtual(name, desc);
        }
        if (accessRule == null) {
            // 判断访问的方法是否是新方法
            MethodBindInfo bindInfo = MethodBindManager.getBindInfo(ClassUtil.classpath2name(owner), name, desc, opcode == Opcodes.INVOKESTATIC);
            if (bindInfo != null) accessRule = bindInfo.getAccessRule(true);
            else accessRule = MethodAccessRule.direct();
        }
        accessRule.access(insnList, opcode, owner, name, desc, isInterface);
    }

    public void accessField(InsnList insnList, int opcode, String owner, String name, String type) {
        FieldAccessRule accessRule = findFieldAccessRule(ClassFieldUniqueDesc.of(ClassUtil.classpath2name(owner), name, type));
        if (accessRule == null) {
            accessRule = FieldAccessRule.direct();
        }
        accessRule.access(insnList, opcode, owner, name, type);
    }

    private MethodAccessRule findMethodVirtual(String name, String desc) {
        for (Map.Entry<ClassMethodUniqueDesc, MethodAccessRule> method : methodAccessRules.entrySet()) {
            ClassMethodUniqueDesc key = method.getKey();
            if (key.getName().equals(name) && key.getDesc().equals(desc)) {
                return method.getValue();
            }
        }
        return null;
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
}
