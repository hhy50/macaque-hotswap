package six.eared.macaque.agent.asm2.classes;

public interface ClazzDefinitionVisitorFactory {

    ClazzDefinitionVisitorFactory DEFAULT = new Default();

    /**
     * @return
     */
    public ClazzDefinitionVisitor creatClazzVisitor();

    class Default implements ClazzDefinitionVisitorFactory {

        private Default() {

        }

        @Override
        public ClazzDefinitionVisitor creatClazzVisitor() {
            return new ClazzDefinitionVisitor();
        }
    }
}
