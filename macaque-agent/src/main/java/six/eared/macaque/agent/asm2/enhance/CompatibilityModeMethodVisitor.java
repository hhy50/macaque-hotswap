package six.eared.macaque.agent.asm2.enhance;

import six.eared.macaque.agent.annotation.VisitEnd;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.classes.AsmMethodHolder;
import six.eared.macaque.agent.asm2.classes.AsmMethodVisitor;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.asm2.classes.MethodVisitorProxy;
import six.eared.macaque.agent.exceptions.EnhanceException;
import six.eared.macaque.agent.vcs.VersionChainAccessor;
import six.eared.macaque.asm.*;
import six.eared.macaque.common.util.ClassUtil;
import six.eared.macaque.common.util.CollectionUtil;

import java.util.ArrayList;
import java.util.List;


public class CompatibilityModeMethodVisitor implements AsmMethodVisitor {

    private List<AsmMethodHolder> newMethods;

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
                    return writer.visitMethod(method.getModifier(), method.getMethodName(), method.getDesc(), method.getMethodSign(), method.getExceptions());
                } else {
                    if (this.newMethods == null) {
                        this.newMethods = new ArrayList<>();
                    }
                    MethodVisitorProxy methodVisitorProxy = new MethodVisitorProxy();
                    this.newMethods.add(new AsmMethodHolder(method, methodVisitorProxy));
                    return methodVisitorProxy;
                }
            }
        } catch (Exception e) {
            throw new EnhanceException(e);
        }
        return null;
    }

    @VisitEnd
    public void visitEnd() {
        if (CollectionUtil.isNotEmpty(newMethods)) {
            this.classNameGenerator = this.classNameGenerator == null ? new SimpleClassNameGenerator() : this.classNameGenerator;
            for (AsmMethodHolder newMethod : newMethods) {
                AsmMethod asmMethod = newMethod.getAsmMethod();
                MethodVisitorProxy methodVisitor = newMethod.getVisitor();
                String bindClassName = this.classNameGenerator.generate(clazzDefinition.getClassName(), asmMethod.getMethodName());
                ClassWriter cw = new ClassWriter(0);
                cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, bindClassName, null, "java/lang/Object", null);
                methodVisitor.revisit(cw.visitMethod(asmMethod.getModifier(), asmMethod.getMethodName(), asmMethod.getDesc(),
                        asmMethod.getMethodSign(), asmMethod.getExceptions()));
                cw.visitEnd();
                CompatibilityModeClassLoader.loadClass(bindClassName, cw.toByteArray());
            }
        }
    }

    public ClassWriter generateClass() {
        return new ClassWriter(0);
    }

    public ClassNameGenerator getClassNameGenerator() {
        return classNameGenerator;
    }

    public void setClassNameGenerator(ClassNameGenerator classNameGenerator) {
        this.classNameGenerator = classNameGenerator;
    }
}
