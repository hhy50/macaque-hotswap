package six.eared.macaque.agent.asm2.classes;

import six.eared.macaque.agent.asm2.enhance.CompatibilityModeMethodVisitor;

public interface ClazzDefinitionVisitorFactory {

    ClazzDefinitionVisitorFactory DEFAULT = new Default();

    ClazzDefinitionVisitorFactory COMPATIBILITY_MODE = new CompatibilityMode();

    /**
     *
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

    class CompatibilityMode implements ClazzDefinitionVisitorFactory {

        private CompatibilityMode() {

        }

        @Override
        public ClazzDefinitionVisitor creatClazzVisitor() {
            return new ClazzDefinitionVisitor(new CompatibilityModeMethodVisitor(), null);
        }
    }
}
