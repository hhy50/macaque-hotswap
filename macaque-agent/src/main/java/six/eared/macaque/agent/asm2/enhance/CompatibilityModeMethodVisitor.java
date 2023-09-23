package six.eared.macaque.agent.asm2.enhance;

import six.eared.macaque.agent.annotation.VisitEnd;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.classes.*;
import six.eared.macaque.agent.exceptions.EnhanceException;
import six.eared.macaque.agent.vcs.VersionChainAccessor;
import six.eared.macaque.asm.ClassWriter;
import six.eared.macaque.asm.MethodVisitor;
import six.eared.macaque.asm.Opcodes;
import six.eared.macaque.common.util.ClassUtil;
import six.eared.macaque.common.util.CollectionUtil;

import java.util.HashSet;
import java.util.Set;


public class CompatibilityModeMethodVisitor implements AsmMethodVisitor {

    private Set<AsmMethodHolder> newMethods = new HashSet<>();

    private ClazzDefinition clazzDefinition;

    private ClassNameGenerator classNameGenerator;

    public CompatibilityModeMethodVisitor() {
    }

    @Override
    public MethodVisitor visitMethod(AsmMethod method, ClazzDefinition clazzDefinition, ClassWriter writer) {
        this.clazzDefinition = clazzDefinition;
        try {
            String className = clazzDefinition.getClassName();
            ClazzDefinition lastVersion = VersionChainAccessor.findLastView(className);
            if (lastVersion != null) {
                if (lastVersion.hasMethod(method)) {
                    clazzDefinition.addAsmMethod(method);
                    return writer.visitMethod(method.getModifier(), method.getMethodName(), method.getDesc(),
                            method.getMethodSign(), method.getExceptions());
                } else {
                    return holdNewMethodCaller(method);
                }
            }
        } catch (Exception e) {
            throw new EnhanceException(e);
        }
        return null;
    }

    private MethodVisitor holdNewMethodCaller(AsmMethod method) {
        AsmMethodVisitorCaller caller = new AsmMethodVisitorCaller();

        AsmMethodHolder holder = new AsmMethodHolder();
        holder.setCaller(caller);
        holder.setAsmMethod(method);

        this.newMethods.add(holder);
        return new MethodVisitorProxy(caller.createProxyObj());
    }

    @VisitEnd
    public void visitEnd() {
        if (CollectionUtil.isNotEmpty(newMethods)) {
            this.classNameGenerator = this.classNameGenerator == null ? new SimpleClassNameGenerator() : this.classNameGenerator;
            for (AsmMethodHolder newMethod : newMethods) {
                bindNewMethodToNewClass(newMethod);
                this.clazzDefinition.addAsmMethod(newMethod.getAsmMethod());
            }
        }
    }

    public void bindNewMethodToNewClass(AsmMethodHolder newMethod) {
        AsmMethod asmMethod = newMethod.getAsmMethod();
        AsmMethodVisitorCaller caller = newMethod.getCaller();

        String bindMethodName = asmMethod.getMethodName();
        String bindClassName = this.classNameGenerator.generate(this.clazzDefinition.getClassName(), bindMethodName);
        MethodBindInfo methodBindInfo = new MethodBindInfo();
        methodBindInfo.setBindClass(bindClassName);
        methodBindInfo.setBindMethod(bindMethodName);

        // load the bind class
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, ClassUtil.simpleClassName2path(methodBindInfo.getBindClass()), null, "java/lang/Object", null);
        caller.accept(cw.visitMethod(asmMethod.getModifier() | Opcodes.ACC_STATIC, methodBindInfo.getBindMethod(), asmMethod.getDesc(),
                asmMethod.getMethodSign(), asmMethod.getExceptions()));
        cw.visitEnd();
        CompatibilityModeClassLoader.loadClass(bindClassName, cw.toByteArray());

        asmMethod.setMethodBindInfo(methodBindInfo);
    }

    public ClassNameGenerator getClassNameGenerator() {
        return classNameGenerator;
    }

    public void setClassNameGenerator(ClassNameGenerator classNameGenerator) {
        this.classNameGenerator = classNameGenerator;
    }
}
