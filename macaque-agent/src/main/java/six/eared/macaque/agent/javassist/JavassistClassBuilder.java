package six.eared.macaque.agent.javassist;

import javassist.*;
import javassist.bytecode.*;
import lombok.Getter;
import lombok.SneakyThrows;
import org.objectweb.asm.Type;
import six.eared.macaque.agent.asm2.AsmUtil;

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

    public JavassistClassBuilder defineMethod(String src, Consumer<Bytecode> interceptor) throws CannotCompileException, BadBytecode, NotFoundException {
        CtMethod ctMethod = this.defineMethod(src);
        MethodInfo methodInfo = ctMethod.getMethodInfo();

        Bytecode bytecode = new Bytecode(ctClass.getClassFile().getConstPool());
        interceptor.accept(bytecode);
        CodeAttribute codeAttr = bytecode.toCodeAttribute();
        codeAttr.computeMaxStack();
        codeAttr.setMaxLocals(AsmUtil.calculateLvbOffset((methodInfo.getAccessFlags()&AccessFlag.STATIC)>0, Type.getMethodType(methodInfo.getDescriptor()).getArgumentTypes()));
        methodInfo.setCodeAttribute(codeAttr);
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
