package six.eared.macaque.agent.asm2.enhance;

import six.eared.macaque.agent.asm2.AsmField;
import six.eared.macaque.agent.asm2.classes.AsmFieldVisitor;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.asm.ClassWriter;
import six.eared.macaque.asm.FieldVisitor;

public class CompatibilityModeFieldVisitor implements AsmFieldVisitor {


    @Override
    public FieldVisitor visitField(AsmField field, ClazzDefinition clazzDefinition, ClassWriter writer) {
        return writer.visitField(field.getModifier(), field.getFieldName(), field.getFieldDesc(), field.getFieldSign(), field.getValue());
    }
}