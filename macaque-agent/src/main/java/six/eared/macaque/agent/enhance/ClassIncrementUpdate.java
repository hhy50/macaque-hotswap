package six.eared.macaque.agent.enhance;

import lombok.Data;
import six.eared.macaque.agent.asm2.AsmField;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.asm2.classes.CorrelationClazzDefinition;
import six.eared.macaque.agent.definition.Definition;
import six.eared.macaque.agent.enums.CorrelationEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * 类的增量信息
 */

@Data
public class ClassIncrementUpdate implements Definition {

    private String className;

    /**
     * 增强之前的类
     */
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
     * 删除的字段
     */
    public List<AsmField> deletedFields;

    /**
     * 相关联的其他类
     */
    private List<CorrelationClazzDefinition> correlationClasses;

    private byte[] enhancedByteCode;

    public ClassIncrementUpdate(ClazzDefinition definition) {
        this.className = definition.getClassName();
        this.clazzDefinition = definition;
    }

    public void addDeletedMethod(AsmMethod deletedMethod) {
        if (this.deletedMethods == null) {
            this.deletedMethods = new ArrayList<>();
        }
        this.deletedMethods.add(deletedMethod);
    }

    public void addNewMethod(AsmMethod newMethod) {
        if (this.newMethods == null) {
            this.newMethods = new ArrayList<>();
        }
        this.newMethods.add(newMethod);
    }

    public void addDeletedField(AsmField deletedField) {
        if (this.deletedFields == null) {
            this.deletedFields = new ArrayList<>();
        }
        this.deletedFields.add(deletedField);
    }


    public void addCorrelationClasses(CorrelationEnum correlation, ClazzDefinition definition) {
        if (this.correlationClasses == null) {
            this.correlationClasses = new ArrayList<>();
        }
        this.correlationClasses.add(CorrelationClazzDefinition.of(correlation, definition));
    }

    @Override
    public String getName() {
        return className;
    }

    @Override
    public String getFileType() {
        return "class";
    }

    @Override
    public byte[] getByteArray() {
        return enhancedByteCode;
    }
}
