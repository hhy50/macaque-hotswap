package six.eared.macaque.agent.javassist;

import javassist.*;
import javassist.bytecode.Bytecode;
import javassist.bytecode.MethodInfo;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.function.Consumer;

import static six.eared.macaque.agent.javassist.JavaSsistUtil.POOL;

public class JavassistClassBuilder {

    @Getter
    protected final String className;

    protected CtClass ctClass;

    protected JavassistClassBuilder(int modifier, String className, String superClass, String[] interfaces)
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

    public JavassistClassBuilder defineConstructor(String src) throws CannotCompileException {
        this.ctClass.addConstructor(CtNewConstructor.make(src, ctClass));
        return this;
    }

    public JavassistClassBuilder defineField(String src) throws CannotCompileException {
        this.ctClass.addField(CtField.make(src, this.ctClass));
        return this;
    }

    public CtMethod defineMethod(String src) throws CannotCompileException {
        CtMethod ctMethod = CtMethod.make(src, ctClass);
        this.ctClass.addMethod(ctMethod);
        return ctMethod;
    }

    public JavassistClassBuilder defineMethod(String src, Consumer<Bytecode> interceptor) throws CannotCompileException {
        CtMethod ctMethod = this.defineMethod(src);

        Bytecode bytecode = new Bytecode(ctClass.getClassFile().getConstPool());
        interceptor.accept(bytecode);

        MethodInfo methodInfo = ctMethod.getMethodInfo();
        methodInfo.setCodeAttribute(bytecode.toCodeAttribute());
        return this;
    }

    static CtClass[] getTypes(String[] types) throws NotFoundException {
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
