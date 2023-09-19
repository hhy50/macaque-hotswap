package six.eared.macaque.agent.asm2.classes;

import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.asm.ClassWriter;
import six.eared.macaque.asm.MethodVisitor;


public interface AsmMethodVisitor {

    /**
     *
     * @return
     */
    public MethodVisitor visitMethod(AsmMethod method, ClazzDefinition clazzDefinition, ClassWriter writer);

    /**
     *
     * @return
     */
    public void visitEnd();
}
