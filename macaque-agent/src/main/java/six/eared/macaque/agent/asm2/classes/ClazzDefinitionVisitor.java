package six.eared.macaque.agent.asm2.classes;


import six.eared.macaque.asm.ClassVisitor;
import six.eared.macaque.asm.Opcodes;

public class ClazzDefinitionVisitor extends ClassVisitor {

    private ClazzDefinition definition = null;

    public ClazzDefinitionVisitor() {
        super(Opcodes.ASM4);
    }

    public ClazzDefinition getDefinition() {
        return definition;
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        definition = new ClazzDefinition();
        definition.setClassName(name.replaceAll("/", "."));
    }

    @Override
    public void visitBytes(byte[] bytes) {
        definition.setClassData(bytes);
    }

    public void visitEnd() {

    }
}
