package six.eared.macaque.agent.enhance;


import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import six.eared.macaque.agent.accessor.Accessor;
import six.eared.macaque.agent.asm2.AsmUtil;

import java.util.HashSet;
import java.util.Set;

import static org.objectweb.asm.Opcodes.*;
import static six.eared.macaque.agent.asm2.AsmUtil.isAload0;


public class BindMethodWriter extends MethodNode {

    private Accessor accessor;

    private boolean isStatic;

    private Set<VarInsnNode> load0Set = new HashSet<>();

    public BindMethodWriter(MethodBindInfo bindInfo, Accessor accessor) {
        super(Opcodes.ASM9);
        this.accessor = accessor;
        this.isStatic = bindInfo.isStatic();
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {

    }

    @Override
    public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {

    }

    @Override
    public void visitLineNumber(int line, Label start) {

    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {

    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        if (owner.contains("$macaque$")) {
            super.visitFieldInsn(opcode, owner, name, desc);
            return;
        }
        if (opcode != Opcodes.GETSTATIC && opcode != Opcodes.PUTSTATIC) {
            // 非静态需要判断操作的变量是否是 slot[0]
            AbstractInsnNode insn = AsmUtil.getPrevStackInsn(opcode == Opcodes.PUTFIELD ? 1 : 0, AsmUtil.getPrevValid(instructions.getLast()));
            if (!isAload0(insn)) {
                super.visitFieldInsn(opcode, owner, name, desc);
                return;
            }
            load0Set.remove(insn);
        }
        accessor.accessField(this.instructions, opcode, owner, name, desc);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isInterface) {
        if (owner.contains("macaque$")) {
            super.visitMethodInsn(opcode, owner, name, desc, isInterface);
            return;
        }
        if (opcode != Opcodes.INVOKESTATIC) {
            // 非静态需要判断操作的变量是否是 slot[0]
            AbstractInsnNode insn = AsmUtil.getPrevStackInsn(Type.getArgumentTypes(desc).length, AsmUtil.getPrevValid(instructions.getLast()));
            if (!isAload0(insn)) {
                super.visitMethodInsn(opcode, owner, name, desc, isInterface);
                return;
            }
            load0Set.remove(insn);
        }
        accessor.accessMethod(this.instructions, opcode, owner, name, desc, isInterface);
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode >= IRETURN && opcode <= RETURN) {
            AbstractInsnNode last = this.instructions.getLast();
            if (isAload0(last)) {
                accessor.accessSelf(this.instructions, last);
                load0Set.remove(last);
            }
        }
        super.visitInsn(opcode);
    }

    @Override
    public void visitVarInsn(int opcode, int varIndex) {
        VarInsnNode loadInst = new VarInsnNode(opcode, varIndex);
        if (!isStatic && opcode == ALOAD && varIndex == 0) {
            load0Set.add(loadInst);
        }
        instructions.add(loadInst);
    }

    @Override
    public void visitEnd() {
        for (VarInsnNode load0 : load0Set) {
            accessor.accessSelf(this.instructions, load0);
        }
        load0Set.clear();
        super.visitEnd();
    }
}
