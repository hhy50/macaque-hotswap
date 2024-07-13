package six.eared.macaque.agent.asm2.classes;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import six.eared.macaque.agent.asm2.AsmMethod;


public interface AsmMethodVisitor {

    /**
     *
     * @return
     */
    public MethodVisitor visitMethod(AsmMethod method, ClazzDefinition clazzDefinition, ClassWriter writer);

    default void visitStart(ClazzDefinition definition) {

    }

    default void visitEnd() {

    }
}
