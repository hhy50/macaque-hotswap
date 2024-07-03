package six.eared.macaque.agent.asm2;

import javassist.*;
import lombok.Getter;
import lombok.SneakyThrows;
import six.eared.macaque.agent.asm2.classes.ClassVisitorDelegation;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.asm2.classes.ClazzDefinitionVisitor;
import six.eared.macaque.asm.ClassReader;
import six.eared.macaque.asm.ClassWriter;
import six.eared.macaque.asm.MethodVisitor;
import six.eared.macaque.asm.Opcodes;
import six.eared.macaque.common.util.ClassUtil;
import six.eared.macaque.common.util.StringUtil;

import java.util.Arrays;
import java.util.stream.Collectors;

import static six.eared.macaque.agent.javassist.JavaSsistUtil.POOL;

public class ClassBuilder {

    @Getter
    private String className;

    private CtClass ctClass;

    private ClassWriter classWriter;

    public ClassBuilder(int modifier, String className, String superClass, String[] interfaces)
            throws NotFoundException, CannotCompileException {
        this.className = className;
        this.ctClass = javassistClass(modifier, className, superClass, interfaces);
        this.classWriter = new ClassWriter(0);
        this.classWriter.visit(Opcodes.V1_8, modifier, ClassUtil.simpleClassName2path(className), null,
                superClass != null ? ClassUtil.simpleClassName2path(superClass) : "java/lang/Object", interfaces);
    }

    private CtClass javassistClass(int modifier, String className, String superClass, String[] interfaces)
            throws NotFoundException, CannotCompileException {
        CtClass ctClass = POOL.makeClass(className);
        ctClass.setModifiers(modifier);

        if (superClass != null) {
            ctClass.setSuperclass(POOL.get(superClass));
        }
        if (interfaces != null && interfaces.length > 0) {
            ctClass.setInterfaces(getTypes(interfaces));
        }
        return ctClass;
    }

//    public ClassBuilder defineClass(int access, String className, String superName, String[] interfaces, String signature) {
//        this.classWriter.visit(Opcodes.V1_8, access, ClassUtil.simpleClassName2path(className), signature,
//                superName != null ? ClassUtil.simpleClassName2path(superName) : "java/lang/Object", interfaces);
//        return this;
//    }

    public ClassBuilder defineField(String src) throws CannotCompileException, NotFoundException {
        this.ctClass.addField(CtField.make(src, this.ctClass));
        return this;
    }

    public ClassBuilder defineField(int modifier, String fieldName, String fieldType) throws CannotCompileException, NotFoundException {
        CtField field = new CtField(POOL.get(fieldType), fieldName, this.ctClass);
        field.setModifiers(modifier);
        this.ctClass.addField(field);
        return this;
    }

    public ClassBuilder defineConstructor(String src) throws CannotCompileException, NotFoundException {
        this.ctClass.addConstructor(CtNewConstructor.make(src, ctClass));
        return this;
    }

    public ClassBuilder defineMethod(String src) throws CannotCompileException {
        this.ctClass.addMethod(CtMethod.make(src, ctClass));
        return this;
    }

    public ClassBuilder defineMethod(int modifier, String rType, String methodName, String[] params)
            throws NotFoundException, CannotCompileException {
        CtMethod ctMethod = new CtMethod(POOL.get(rType), methodName, getTypes(params), this.ctClass);
        ctMethod.setModifiers(modifier);
        this.ctClass.addMethod(ctMethod);
        return this;
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

    public ClassBuilder end() {
        this.classWriter.visitEnd();
        return this;
    }

    private CtClass[] getTypes(String[] types) throws NotFoundException {
        CtClass[] ctClasses = new CtClass[types.length];
        for (int i = 0; i < types.length; i++) {
            ctClasses[i] = POOL.get(types[i]);
        }
        return ctClasses;
    }

    @SneakyThrows
    public byte[] toByteArray() {
        AsmUtil.visitClass(classWriter.toByteArray(), new ClassVisitorDelegation(null) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                return super.visitMethod(access, name, desc, signature, exceptions);
            }
        });
        return this.ctClass.toBytecode();
    }
}
