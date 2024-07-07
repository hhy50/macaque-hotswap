package six.eared.macaque.agent.asm2;

import lombok.Data;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.enhance.MethodBindInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 类的增量信息
 */

@Data
public class ClassIncrementUpdate {

    private ClazzDefinition clazzDefinition;

    /**
     * 新增的
     */
    public Map<AsmMethod, MethodBindInfo> newMethods;

    /**
     * 删除的
     */
    public List<AsmMethod> deletedMethods;

    public ClassIncrementUpdate(ClazzDefinition definition) {
        this.clazzDefinition = definition;
    }

    public void addDeleted(AsmMethod deletedMethod) {
        if (this.deletedMethods == null) {
            this.deletedMethods = new ArrayList<>();
        }
        this.deletedMethods.add(deletedMethod);
    }

    public void addNew(AsmMethod newMethod) {
//        newMethods.put(newMethod, MethodBindInfo.builder().build());
    }
}
