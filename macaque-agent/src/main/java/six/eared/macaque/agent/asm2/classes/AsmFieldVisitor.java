package six.eared.macaque.agent.asm2.classes;

import six.eared.macaque.agent.asm2.AsmField;
import six.eared.macaque.asm.ClassWriter;
import six.eared.macaque.asm.FieldVisitor;

public interface AsmFieldVisitor {

    /**
     *
     * @return
     */
    public FieldVisitor visitField(AsmField field, ClassWriter writer);
}
