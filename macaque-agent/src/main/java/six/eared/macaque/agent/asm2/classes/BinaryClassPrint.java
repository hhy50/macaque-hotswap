package six.eared.macaque.agent.asm2.classes;


import six.eared.macaque.asm.*;

import static six.eared.macaque.asm.Opcodes.ASM4;

import six.eared.macaque.agent.asm2.AsmMethod;

/**
 * 反编译
 */
public class BinaryClassPrint extends ClassVisitor {
    public BinaryClassPrint() {
        super(ASM4);
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        System.out.println(name + " extends " + superName + " {");
    }

    public void visitSource(String source, String debug) {
    }

    public void visitOuterClass(String owner, String name, String desc) {
    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return null;
    }

    public void visitAttribute(Attribute attr) {
    }

    public void visitInnerClass(String name, String outerName, String innerName, int access) {
    }

    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        System.out.println("    " + desc + " " + name);
        return null;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        System.out.println("    " + name + desc);
        AsmMethod asmMethod = new AsmMethod();
        return new AsmMethodReader(asmMethod);
    }

    public void visitEnd() {
        System.out.println("}");
    }
}
