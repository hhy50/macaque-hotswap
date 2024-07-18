package six.eared.macaque.agent.enhance;

import lombok.Data;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.asm2.classes.CorrelationClazzDefinition;
import six.eared.macaque.agent.enums.CorrelationEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * 类更新的增量信息
 */

@Data
public class ClassIncrementUpdate {

    private String className;

    /**
     * 增强之前的类
     */
    private ClazzDataDefinition clazzDefinition;

    /**
     * 访问器
     */
    private ClazzDefinition accessor;

    /**
     * 本次更新的方法
     */
    private List<MethodInstance> methods;

    /**
     * 相关联的其他类
     */
    private List<CorrelationClazzDefinition> correlationClasses;

    private byte[] enhancedByteCode;

    public ClassIncrementUpdate(ClazzDataDefinition definition, ClazzDefinition accessor) {
        this.className = definition.getClassName();
        this.clazzDefinition = definition;
        this.accessor = accessor;
    }

    public void addMethod(MethodInstance asmMethod) {
        if (this.methods == null) {
            this.methods = new ArrayList<>();
        }
        this.methods.add(asmMethod);
    }

    public MethodInstance getMethod(String name, String desc) {
        if (this.methods == null) {
            return null;
        }
        return this.methods
                .stream().filter(item -> item.getMethodName().equals(name) && item.getDesc().equals(desc))
                .findAny().orElse(null);
    }

    public void remove(MethodInstance methodInstance) {
        if (this.methods == null) {
            return;
        }
        this.methods.remove(methodInstance);
    }

    public void addCorrelationClasses(CorrelationEnum correlation, ClazzDefinition definition) {
        if (this.correlationClasses == null) {
            this.correlationClasses = new ArrayList<>();
        }
        this.correlationClasses.add(CorrelationClazzDefinition.of(correlation, definition));
    }
}
