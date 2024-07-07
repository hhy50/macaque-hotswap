package six.eared.macaque.agent.enhance;

import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.vcs.VersionChainTool;
import six.eared.macaque.asm.MethodVisitor;
import six.eared.macaque.asm.Opcodes;
import six.eared.macaque.common.util.ClassUtil;

public class MethodByteCodeEnhancer extends MethodVisitor {

    public MethodByteCodeEnhancer(MethodVisitor mv) {
        super(Opcodes.ASM5, mv);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        String targetClassName = ClassUtil.classpath2name(owner);
        ClazzDefinition definition = VersionChainTool.findLastClassVersion(targetClassName, true);
        if (definition != null) {
            AsmMethod method = definition.getMethod(name, desc);
            if (method != null && method.getMethodBindInfo() != null) {
                MethodBindInfo methodBindInfo = method.getMethodBindInfo();
                owner = ClassUtil.simpleClassName2path(methodBindInfo.getBindClass());
                opcode = Opcodes.INVOKESTATIC;
            }
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }
}