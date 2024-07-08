package six.eared.macaque.agent.enhance;

import lombok.Data;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;

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

    public ClassIncrementUpdate(ClazzDefinition definition, ClazzDefinition accessor) {
        this.className = definition.getClassName();
        this.clazzDefinition = definition;
        this.accessorDefinition = accessor;
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
}
