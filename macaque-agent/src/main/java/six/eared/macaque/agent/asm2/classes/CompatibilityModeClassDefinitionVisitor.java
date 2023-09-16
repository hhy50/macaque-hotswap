package six.eared.macaque.agent.asm2.classes;

public class CompatibilityModeClassDefinitionVisitor implements ClazzDefinitionVisitorFactory {

    @Override
    public ClazzDefinitionVisitor creatClazzVisitor() {
        return new ClazzDefinitionVisitor();
    }
}
