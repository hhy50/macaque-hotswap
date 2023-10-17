package six.eared.macaque.agent.asm2.classes;

import six.eared.macaque.agent.enums.CorrelationEnum;

/**
 *
 */
public class CorrelationClazzDefinition {

    private boolean isLoaded;

    private ClazzDefinition clazzDefinition;

    private CorrelationEnum correlation;

    public static CorrelationClazzDefinition bind(CorrelationEnum correlation, ClazzDefinition clazzDefinition) {
        CorrelationClazzDefinition correlationClazzDefinition = new CorrelationClazzDefinition();
        correlationClazzDefinition.clazzDefinition = clazzDefinition;
        correlationClazzDefinition.correlation = correlation;
        return correlationClazzDefinition;
    }

    public CorrelationEnum getCorrelation() {
        return correlation;
    }

    public void setCorrelation(CorrelationEnum correlation) {
        this.correlation = correlation;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public void setLoaded(boolean loaded) {
        isLoaded = loaded;
    }

    public ClazzDefinition getClazzDefinition() {
        return clazzDefinition;
    }
}
