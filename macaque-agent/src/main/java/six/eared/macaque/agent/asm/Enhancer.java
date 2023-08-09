package six.eared.macaque.agent.asm;


import six.eared.macaque.agent.asm.classes.ClassDefinition;

public interface Enhancer {

    /**
     *
     * @param origin
     * @return
     */
    public ClassDefinition enhance(ClassDefinition origin);
}
