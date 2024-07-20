package six.eared.macaque.agent.enhance;

import lombok.Data;
import six.eared.macaque.agent.accessor.Accessor;
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
     * 原生的类
     */
    private ClazzDefinition originDefinition;

    /**
     * 访问器
     */
    private Accessor accessor;

    /**
     * 本次更新的方法
     */
    private List<MethodUpdateInfo> methods;

    /**
     * 本次更新的字段
     */
    private List<FieldUpdateInfo> fields;

    /**
     * 相关联的其他类
     */
    private List<CorrelationClazzDefinition> correlationClasses;

    private byte[] enhancedByteCode;

    public ClassIncrementUpdate(ClazzDataDefinition definition,ClazzDefinition originDefinition, Accessor accessor) {
        this.className = definition.getClassName();
        this.clazzDefinition = definition;
        this.originDefinition = originDefinition;
        this.accessor = accessor;
    }

    public void addMethod(MethodUpdateInfo asmMethod) {
        if (this.methods == null) {
            this.methods = new ArrayList<>();
        }
        this.methods.add(asmMethod);
    }

    public void addField(FieldUpdateInfo fieldUpdateInfo) {
        if (this.fields == null) {
            this.fields = new ArrayList<>();
        }
        this.fields.add(fieldUpdateInfo);
    }

    public MethodUpdateInfo getMethod(String name, String desc) {
        if (this.methods == null) {
            return null;
        }
        return this.methods
                .stream().filter(item -> item.getMethodName().equals(name) && item.getDesc().equals(desc))
                .findAny().orElse(null);
    }

    public FieldUpdateInfo getField(String fieldName, String desc) {
        if (this.fields == null) {
            return null;
        }
        return this.fields
                .stream().filter(item -> item.getFieldName().equals(fieldName) && item.getDesc().equals(desc))
                .findAny().orElse(null);
    }

    public void remove(MethodUpdateInfo methodUpdateInfo) {
        if (this.methods == null) {
            return;
        }
        this.methods.remove(methodUpdateInfo);
    }

    public void remove(FieldUpdateInfo fieldUpdateInfo) {
        if (this.fields == null) {
            return;
        }
        this.fields.remove(fieldUpdateInfo);
    }

    public void addCorrelationClasses(CorrelationEnum correlation, ClazzDefinition definition) {
        if (this.correlationClasses == null) {
            this.correlationClasses = new ArrayList<>();
        }
        this.correlationClasses.add(CorrelationClazzDefinition.of(correlation, definition));
    }
}
