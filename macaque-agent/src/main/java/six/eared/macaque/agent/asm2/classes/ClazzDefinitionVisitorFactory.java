package six.eared.macaque.agent.asm2.classes;

public interface ClazzDefinitionVisitorFactory {

    /**
     *
     * @return
     */
    public ClazzDefinitionVisitor creatClazzVisitor();


    class Default implements ClazzDefinitionVisitorFactory {
        @Override
        public ClazzDefinitionVisitor creatClazzVisitor() {
            return new ClazzDefinitionVisitor();
        }
    }
}
