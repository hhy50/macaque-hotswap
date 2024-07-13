package six.eared.macaque.agent.asm2;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.common.util.ClassUtil;
import six.eared.macaque.common.util.StringUtil;

import java.util.Arrays;
import java.util.stream.Collectors;

public class AsmClassBuilder {


    private String className;

    private String superClassName;

    private ClassWriter classWriter = new ClassWriter(0);

    public AsmClassBuilder() {

    }

    public AsmClassBuilder defineClass(int access, String className, String superName, String[] interfaces, String signature) {
        this.classWriter.visit(Opcodes.V1_8, access, ClassUtil.simpleClassName2path(className), signature,
                superName != null ? ClassUtil.simpleClassName2path(superName) : "java/lang/Object", interfaces);
        this.className = className;
        this.superClassName = superName;
        return this;
    }

    public AsmClassBuilder defineField(int access, String fieldName, String fieldDesc, String fieldSignature, Object value) {
        this.classWriter.visitField(access, fieldName, fieldDesc, fieldSignature, value);
        return this;
    }

    public MethodBuilder defineConstruct(int access, String[] argsType, String[] exceptions, String sign) {
        MethodVisitor methodVisitor = this.classWriter.visitMethod(access, "<init>", "(" + toDesc(argsType) + ")V", sign, exceptions);
        return new MethodBuilder(this, methodVisitor);
    }

    public MethodBuilder defineMethod(int access, String methodName, String methodDesc, String[] exceptions, String methodSign) {
        MethodVisitor methodVisitor = this.classWriter.visitMethod(access, methodName, methodDesc, methodSign, exceptions);
        return new MethodBuilder(this, methodVisitor);
    }

    private static String toDesc(String[] types) {
        String typeDesc = StringUtil.EMPTY_STR;
        if (types != null && types.length > 0) {
            typeDesc = Arrays.stream(types).map(AsmUtil::toTypeDesc).collect(Collectors.joining());
        }
        return typeDesc;
    }

    public AsmClassBuilder end() {
        this.classWriter.visitEnd();
        return this;
    }

    public ClazzDefinition toDefinition() {
        return AsmUtil.readClass(this.classWriter.toByteArray());
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSuperClassName() {
        return superClassName;
    }

    public void setSuperClassName(String superClassName) {
        this.superClassName = superClassName;
    }
}
