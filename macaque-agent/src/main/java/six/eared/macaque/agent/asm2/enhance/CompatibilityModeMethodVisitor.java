package six.eared.macaque.agent.asm2.enhance;

import six.eared.macaque.agent.annotation.VisitEnd;
import six.eared.macaque.agent.annotation.VisitStart;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.ClassBuilder;
import six.eared.macaque.agent.asm2.classes.*;
import six.eared.macaque.agent.exceptions.EnhanceException;
import six.eared.macaque.agent.vcs.VersionChainTool;
import six.eared.macaque.asm.ClassWriter;
import six.eared.macaque.asm.MethodVisitor;
import six.eared.macaque.asm.Opcodes;
import six.eared.macaque.common.util.CollectionUtil;

import java.util.HashSet;
import java.util.Set;

public class CompatibilityModeMethodVisitor implements AsmMethodVisitor {

    private Set<AsmMethodHolder> methodHolds = new HashSet<>();

    private ClazzDefinition clazzDefinition;

    private ClassNameGenerator classNameGenerator = new SimpleClassNameGenerator();

    public CompatibilityModeMethodVisitor() {
    }

    @Override
    public MethodVisitor visitMethod(AsmMethod method, ClazzDefinition clazzDefinition, ClassWriter writer) {
        try {
            String className = clazzDefinition.getClassName();
            ClazzDefinition lastDefinition = VersionChainTool.findLastClassVersion(className, true);
            if (lastDefinition == null) {
                lastDefinition = AsmUtil.readOriginClass(className);
            }
            if (lastDefinition != null) {
                if (lastDefinition.hasMethod(method.getMethodName(), method.getDesc())) {
                    clazzDefinition.addAsmMethod(method);
                    AsmMethod prevVersionMethod = lastDefinition.getMethod(method.getMethodName(), method.getDesc());
                    if (prevVersionMethod.getMethodBindInfo() == null) {
                        return writer.visitMethod(method.getModifier(), method.getMethodName(), method.getDesc(),
                                method.getMethodSign(), method.getExceptions());
                    }
                    method.setMethodBindInfo(prevVersionMethod.getMethodBindInfo());
                }
                return holdMethodCaller(method);
            }
        } catch (Exception e) {
            throw new EnhanceException(e);
        }
        return null;
    }


    private MethodVisitor holdMethodCaller(AsmMethod method) {
        AsmMethodVisitorCaller caller = new AsmMethodVisitorCaller();

        AsmMethodHolder holder = new AsmMethodHolder();
        holder.setCaller(caller);
        holder.setAsmMethod(method);
        this.methodHolds.add(holder);
        return caller.createProxyObj();
    }

    @VisitEnd
    public void visitEnd() {
        if (CollectionUtil.isNotEmpty(methodHolds)) {
            for (AsmMethodHolder methodHolder  : methodHolds) {
                // Bind before adding
                bindNewMethodToNewClass(methodHolder);
                this.clazzDefinition.addAsmMethod(methodHolder.getAsmMethod());
            }
        }
    }

    @VisitStart
    public void visitStart(ClazzDefinition definition) {
        this.clazzDefinition = definition;
        int depth = 3;
        CompatibilityModeAccessorUtil.createAccessor(this.clazzDefinition.getClassName(), this.classNameGenerator, depth);
    }


    public void bindNewMethodToNewClass(AsmMethodHolder newMethod) {
        AsmMethod asmMethod = newMethod.getAsmMethod();
        AsmMethodVisitorCaller caller = newMethod.getCaller();

        String bindMethodName = asmMethod.getMethodName();
        String bindClassName = this.classNameGenerator.generate(this.clazzDefinition.getClassName(), bindMethodName);
        MethodBindInfo methodBindInfo = new MethodBindInfo();
        methodBindInfo.setBindClass(bindClassName);
        methodBindInfo.setBindMethod(bindMethodName);
        asmMethod.setMethodBindInfo(methodBindInfo);

        // load the bind class
        ClassBuilder classBuilder = AsmUtil.defineClass(Opcodes.ACC_PUBLIC, methodBindInfo.getBindClass(), null, null, null)
                .defineMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, methodBindInfo.getBindMethod(), asmMethod.getDesc(), null, null)
                .accept(caller::accept)
                .end();
        CompatibilityModeClassLoader.loadClass(bindClassName, classBuilder.toByteArray());
    }

    public ClassNameGenerator getClassNameGenerator() {
        return classNameGenerator;
    }

    public void setClassNameGenerator(ClassNameGenerator classNameGenerator) {
        this.classNameGenerator = classNameGenerator;
    }
}
