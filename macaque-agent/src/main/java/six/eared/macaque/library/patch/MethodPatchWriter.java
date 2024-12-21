package six.eared.macaque.library.patch;


import io.github.hhy50.linker.define.MethodDescriptor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import six.eared.macaque.agent.asm2.AsmUtil;

import static org.objectweb.asm.Opcodes.ASM9;
import static org.objectweb.asm.Opcodes.IRETURN;

public class MethodPatchWriter extends MethodVisitor {
    public MethodPatchWriter() {
        super(ASM9);
    }

    public static void patchMethod(MethodVisitor mv, Type patchedMType, boolean isStatic,
                                   MethodDescriptor delegationMd) {
        // Method with invokedynamic
        mv.visitCode();

        if (!isStatic)
            mv.visitVarInsn(Opcodes.ALOAD, 0);
        AsmUtil.loadArgs(mv, patchedMType.getArgumentTypes(), isStatic);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, delegationMd.getOwner(), delegationMd.getMethodName(), delegationMd.getDesc());
        mv.visitInsn(patchedMType.getReturnType().getOpcode(IRETURN));
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
}
