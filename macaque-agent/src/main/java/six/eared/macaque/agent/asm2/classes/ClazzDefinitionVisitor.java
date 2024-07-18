package six.eared.macaque.agent.asm2.classes;


import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import six.eared.macaque.agent.asm2.AsmField;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.enhance.ClazzDataDefinition;
import six.eared.macaque.common.util.StringUtil;


public class ClazzDefinitionVisitor extends ClassVisitor {

    private ClazzDataDefinition definition = null;

    public ClazzDefinitionVisitor() {
        super(Opcodes.ASM9);
    }

    public ClazzDataDefinition getDefinition() {
        return this.definition;
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.definition = new ClazzDataDefinition();
        this.definition.setClassName(name.replaceAll("/", "."));
        if (StringUtil.isNotEmpty(superName)) {
            this.definition.setSuperClassName(superName.replaceAll("/", "."));
        }
        this.definition.setInterfaces(interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        AsmField asmField = AsmField.AsmFieldBuilder
                .builder()
                .modifier(access)
                .fieldName(name)
                .fieldDesc(desc)
                .fieldSign(signature)
                .value(value)
                .build();
        this.definition.addAsmField(asmField);
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        AsmMethod asmMethod = AsmMethod.AsmMethodBuilder
                .builder()
                .modifier(access)
                .methodName(name)
                .desc(desc)
                .methodSign(signature)
                .exceptions(exceptions)
                .build();
        this.definition.addAsmMethod(asmMethod);
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    /**
     *
     */
    public void setByteCode(byte[] byteCode) {
        this.definition.setBytecode(byteCode);
    }
}
