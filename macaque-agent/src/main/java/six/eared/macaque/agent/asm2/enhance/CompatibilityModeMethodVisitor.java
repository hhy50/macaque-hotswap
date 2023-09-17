package six.eared.macaque.agent.asm2.enhance;

import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.classes.AsmMethodVisitor;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.vcs.VersionChainAccessor;
import six.eared.macaque.asm.ClassWriter;
import six.eared.macaque.asm.MethodVisitor;


public class CompatibilityModeMethodVisitor implements AsmMethodVisitor {

    @Override
    public MethodVisitor visitMethod(AsmMethod method, ClazzDefinition clazzDefinition, ClassWriter writer) {
        String className = clazzDefinition.getClassName();
        ClazzDefinition lastVersion = VersionChainAccessor.findLastView(className);
        if (lastVersion != null) {
            if (lastVersion.hasMethod(method.getMethodName())) {
                clazzDefinition.addAsmMethod(method);
                return writer.visitMethod(method.getModifier(), method.getMethodName(), method.getDesc(), method.getMethodSign(), method.getExceptions());
            }
        }
        return null;
    }
}
