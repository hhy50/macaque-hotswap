package six.eared.macaque.agent.asm2.enhance;

import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.classes.AsmMethodVisitor;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.asm.ClassWriter;
import six.eared.macaque.asm.MethodVisitor;


public class CompatibilityModeMethodVisitor implements AsmMethodVisitor {

    @Override
    public MethodVisitor visitMethod(AsmMethod method, ClazzDefinition definition, ClassWriter writer) {
        return null;
    }
}
