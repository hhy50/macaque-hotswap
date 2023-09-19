package six.eared.macaque.agent.asm2.enhance;

import six.eared.macaque.agent.annotation.VisitEnd;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.classes.AsmMethodHolder;
import six.eared.macaque.agent.asm2.classes.AsmMethodVisitor;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.asm2.classes.MethodVisitorProxy;
import six.eared.macaque.agent.vcs.VersionChainAccessor;
import six.eared.macaque.asm.ClassWriter;
import six.eared.macaque.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.List;


public class CompatibilityModeMethodVisitor implements AsmMethodVisitor {

    private List<AsmMethodHolder> newMethods;

    @Override
    public MethodVisitor visitMethod(AsmMethod method, ClazzDefinition clazzDefinition, ClassWriter writer) {
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
        return null;
    }

    @VisitEnd
    public void visitEnd() {
        System.out.println(newMethods);
    }
}
