package six.eared.macaque.agent.enhance;

import lombok.Data;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.asm2.classes.CorrelationClazzDefinition;
import six.eared.macaque.agent.enums.CorrelationEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * 类的增量信息
 */

@Data
public class ClassIncrementUpdate {

    private String className;

    private ClazzDefinition clazzDefinition;

    /**
     * 访问器
     */
    private ClazzDefinition accessorDefinition;

    /**
     * 新增的
     */
    public List<AsmMethod> newMethods;

    /**
     * 删除的
     */
    public List<AsmMethod> deletedMethods;

    /**
     * 相关联的其他类
     */
    private List<CorrelationClazzDefinition> correlationClasses;

    private byte[] enhancedByteCode;

    public ClassIncrementUpdate(ClazzDefinition definition) {
        this.className = definition.getClassName();
        this.clazzDefinition = definition;
    }

    public void addDeleted(AsmMethod deletedMethod) {
        if (this.deletedMethods == null) {
            this.deletedMethods = new ArrayList<>();
        }
        this.deletedMethods.add(deletedMethod);
    }

    public void addNew(AsmMethod newMethod) {
        if (this.newMethods == null) {
            this.newMethods = new ArrayList<>();
        }
        this.newMethods.add(newMethod);
    }

    public void addCorrelationClasses(CorrelationEnum correlation, ClazzDefinition definition) {
        if (this.correlationClasses == null) {
            this.correlationClasses = new ArrayList<>();
        }
        this.correlationClasses.add(CorrelationClazzDefinition.of(correlation, definition));
    }
}
