package six.eared.macaque.agent.asm2.classes;

import lombok.Data;
import six.eared.macaque.agent.enums.CorrelationEnum;

/**
 *
 */

@Data
public class CorrelationClazzDefinition {

    private ClazzDefinition clazzDefinition;

    private CorrelationEnum correlation;

    public static CorrelationClazzDefinition of(CorrelationEnum correlation, ClazzDefinition clazzDefinition) {
        CorrelationClazzDefinition correlationClazzDefinition = new CorrelationClazzDefinition();
        correlationClazzDefinition.clazzDefinition = clazzDefinition;
        correlationClazzDefinition.correlation = correlation;
        return correlationClazzDefinition;
    }
}
