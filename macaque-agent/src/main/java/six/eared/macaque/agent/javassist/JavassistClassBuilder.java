package six.eared.macaque.agent.javassist;

import javassist.*;
import lombok.Getter;
import lombok.SneakyThrows;

import static six.eared.macaque.agent.javassist.JavaSsistUtil.POOL;

public class JavassistClassBuilder {

    @Getter
    private String className;

    private CtClass ctClass;

    public JavassistClassBuilder(int modifier, String className, String superClass, String[] interfaces)
            throws NotFoundException, CannotCompileException {
        this.className = className;
        this.ctClass = javassistClass(modifier, className, superClass, interfaces);
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

    public JavassistClassBuilder defineField(String src) throws CannotCompileException {
        this.ctClass.addField(CtField.make(src, this.ctClass));
        return this;
    }

    public JavassistClassBuilder defineField(int modifier, String fieldName, String fieldType) throws CannotCompileException, NotFoundException {
        CtField field = new CtField(POOL.get(fieldType), fieldName, this.ctClass);
        field.setModifiers(modifier);
        this.ctClass.addField(field);
        return this;
    }

    public JavassistClassBuilder defineConstructor(String src) throws CannotCompileException {
        this.ctClass.addConstructor(CtNewConstructor.make(src, ctClass));
        return this;
    }

    public JavassistClassBuilder defineMethod(String src) throws CannotCompileException {
        this.ctClass.addMethod(CtMethod.make(src, ctClass));
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
        return this.ctClass.toBytecode();
    }
}
