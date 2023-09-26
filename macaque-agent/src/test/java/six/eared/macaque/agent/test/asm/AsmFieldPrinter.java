package six.eared.macaque.agent.test.asm;

import six.eared.macaque.asm.AnnotationVisitor;
import six.eared.macaque.asm.Attribute;
import six.eared.macaque.asm.FieldVisitor;
import six.eared.macaque.asm.TypePath;

import static six.eared.macaque.asm.Opcodes.ASM5;

public class AsmFieldPrinter extends FieldVisitor {

    public AsmFieldPrinter() {
        super(ASM5);
    }

    @Override
    public void visitAttribute(Attribute attr) {
        super.visitAttribute(attr);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
        return super.visitTypeAnnotation(typeRef, typePath, desc, visible);
    }
}
