package six.eared.macaque.agent.asm2.classes;


import six.eared.macaque.agent.asm2.AsmField;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.asm.ClassVisitor;
import six.eared.macaque.asm.FieldVisitor;
import six.eared.macaque.asm.MethodVisitor;
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
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        AsmField asmField = new AsmField();
        definition.addAsmField(asmField);
        return new AsmFieldReader(asmField);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        AsmMethod asmMethod = AsmMethod.AsmMethodBuilder
                .builder()
                .modifier(access)
                .methodName(name)
                .methodSign(signature)
                .build();
        definition.addAsmMethod(asmMethod);
        return new AsmMethodReader(asmMethod);
    }

    @Override
    public void visitBytes(byte[] bytes) {
        definition.setClassData(bytes);
    }

    public void visitEnd() {

    }
}
