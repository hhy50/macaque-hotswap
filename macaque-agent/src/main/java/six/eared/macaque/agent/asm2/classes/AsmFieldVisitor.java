package six.eared.macaque.agent.asm2.classes;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import six.eared.macaque.agent.asm2.AsmField;

public interface AsmFieldVisitor {

    /**
     *
     * @return
     */
    public FieldVisitor visitField(AsmField field, ClazzDefinition clazzDefinition, ClassWriter writer);
}
