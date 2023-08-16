package six.eared.macaque.agent.asm2;


import six.eared.macaque.agent.asm2.classes.ClazzDefinition;


public interface Enhancer {

    /**
     *
     * @param origin
     * @return
     */
    public ClazzDefinition enhance(ClazzDefinition origin);
}
