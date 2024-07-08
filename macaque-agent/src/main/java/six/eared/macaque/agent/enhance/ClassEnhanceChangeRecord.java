package six.eared.macaque.agent.enhance;

import lombok.Data;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.asm2.classes.CorrelationClazzDefinition;
import six.eared.macaque.agent.enums.CorrelationEnum;

import java.util.ArrayList;
import java.util.List;


@Data
public class ClassEnhanceChangeRecord {

    private final String className;
    private final ClazzDefinition clazzDefinition;
    private final byte[] enhancedByteCode;
    private List<CorrelationClazzDefinition> correlationClasses;


    public ClassEnhanceChangeRecord(String className, ClazzDefinition clazzDefinition, byte[] enhancedByteCode) {
        this.className = className;
        this.clazzDefinition = clazzDefinition;
        this.enhancedByteCode = enhancedByteCode;
    }


    public void addCorrelationClasses(CorrelationEnum correlation, ClazzDefinition definition) {
        if (this.correlationClasses == null) {
            this.correlationClasses = new ArrayList<>();
        }
        this.correlationClasses.add(CorrelationClazzDefinition.of(correlation, definition));
    }
}
