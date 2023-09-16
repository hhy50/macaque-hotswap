package six.eared.macaque.agent.test.asm;

import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.asm.Attribute;
import six.eared.macaque.asm.Handle;
import six.eared.macaque.asm.Label;
import six.eared.macaque.asm.MethodVisitor;

import java.util.Arrays;

import static six.eared.macaque.asm.Opcodes.ASM5;

public class AsmMethodPrinter extends MethodVisitor {

    private int index = 0;

    private final AsmMethod asmMethod;

    public AsmMethodPrinter(AsmMethod asmMethod) {
        super(ASM5);
        this.asmMethod = asmMethod;
    }

    @Override
    public void visitCode() {
        System.out.println("visitCode...");
        super.visitCode();
    }

    @Override
    public void visitInsn(int opcode) {
        System.out.println(String.format("index=%d, visitInsn(), opcode='%d'",
                index++, opcode));
        super.visitInsn(opcode);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        System.out.println(String.format("index=%d, visitIntInsn(), opcode='%d', operand='%d'",
                index++, opcode, operand));
        super.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        System.out.println(String.format("index=%d, visitFieldInsn(), opcode='%d', owner='%s', name='%s', desc='%s'",
                index++, opcode, owner, name, desc));
        super.visitFieldInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
        System.out.println(String.format("index=%d, visitInvokeDynamicInsn(), name='%s', desc='%s', bsm='%s', bsmArgs='%s'",
                index++, name, desc, bsm.toString(), Arrays.toString(bsmArgs)));
        super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        System.out.println(String.format("index=%d, visitMethodInsn(), opcode='%d', owner='%s', name='%s', desc='%s', itf='%s'",
                index++, opcode, owner, name, desc, Boolean.toString(itf)));
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    @Override
    public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
        System.out.println(String.format("index=%d, visitFrame(), type='%d', nLocal='%d', local='%s', nStack='%d', stack='%s'",
                index++, type, nLocal, Arrays.toString(local), nStack, Arrays.toString(stack)));
        super.visitFrame(type, nLocal, local, nStack, stack);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        System.out.println(String.format("index=%d, visitJumpInsn(), opcode='%d', label='%s'",
                index++, opcode, label.toString()));
        super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        System.out.println(String.format("index=%d, visitIincInsn(), var='%d', increment='%d'",
                index++, var, increment));
        super.visitIincInsn(var, increment);
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        System.out.println(String.format("index=%d, visitLineNumber(), line='%d', start='%s'",
                index++, line, start.toString()));
        super.visitLineNumber(line, start);
    }

    @Override
    public void visitLabel(Label label) {
        System.out.println(String.format("index=%d, visitLabel(), label='%s'",
                index++, label));
        super.visitLabel(label);
    }

    @Override
    public void visitLdcInsn(Object cst) {
        System.out.println(String.format("index=%d, visitLdcInsn(), cst='%s'",
                index++, cst.toString()));
        super.visitLdcInsn(cst);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        System.out.println(String.format("index=%d, visitLookupSwitchInsn(), dflt='%s', keys='%s', labels='%s'",
                index++, dflt.toString(), Arrays.toString(keys), Arrays.toString(labels)));
        super.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitMultiANewArrayInsn(String desc, int dims) {
        System.out.println(String.format("index=%d, visitMultiANewArrayInsn(), desc='%s', dims='%d'",
                index++, desc, dims));
        super.visitMultiANewArrayInsn(desc, dims);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        System.out.println(String.format("index=%d, visitTableSwitchInsn(), min='%d', max='%d', dflt='%s', labels='%s'",
                index++, min, max, dflt.toString(), Arrays.toString(labels)));
        super.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        System.out.println(String.format("index=%d, visitTryCatchBlock(), start='%s', end='%s', handler='%s', type='%s'",
                index++, start, end.toString(), handler.toString(), type));
        super.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        System.out.println(String.format("index=%d, visitVarInsn(), opcode='%d', var='%d'",
                index++, opcode, var));
        super.visitVarInsn(opcode, var);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        System.out.println(String.format("index=%d, visitVarInsn(), opcode='%d', type='%s'",
                index++, opcode, type));
        super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitAttribute(Attribute attr) {
        System.out.println(String.format("index=%d, visitAttribute(), attr='%s'",
                index++, attr.toString()));
        super.visitAttribute(attr);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        System.out.println(String.format("index=%d, visitMaxs(), maxStack='%d', maxLocals='%d'",
                index++, maxStack, maxLocals));
        super.visitMaxs(maxStack, maxLocals);
    }

    @Override
    public void visitEnd() {
        System.out.println("visitEnd.....");
        super.visitEnd();
    }
}
