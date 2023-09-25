package six.eared.macaque.agent.asm2.enhance;

import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.vcs.VersionChainTool;
import six.eared.macaque.asm.*;
import six.eared.macaque.common.util.ClassUtil;

import java.util.Arrays;

public class CompatibilityModeByteCodeEnhancer {

    public static byte[] enhance(byte[] bytecode) {
        Enhancer enhancer = new Enhancer();
        ClassReader classReader = new ClassReader(bytecode);
        classReader.accept(enhancer, 0);
        return enhancer.toByteArr();
    }

    static class Enhancer extends ClassVisitor {
        public Enhancer() {
            super(Opcodes.ASM5, new ClassWriter(0));
        }

        public byte[] toByteArr() {
            ClassWriter writer = (ClassWriter) this.cv;
            return writer.toByteArray();
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor writer = super.visitMethod(access, name, desc, signature, exceptions);
            return new MethodByteCodeEnhancer(writer);
        }
    }

    static class MethodByteCodeEnhancer extends MethodVisitor {

        private int index;

        public MethodByteCodeEnhancer(MethodVisitor mv) {
            super(Opcodes.ASM5, mv);
        }
        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            switch (opcode) {
                case Opcodes.INVOKEVIRTUAL:
                    System.out.println("INVOKEVIRTUAL");
                    break;
                case Opcodes.INVOKEDYNAMIC:
                    System.out.println("INVOKEDYNAMIC");
                    break;
                case Opcodes.INVOKEINTERFACE:
                    System.out.println("INVOKEINTERFACE");
                    break;
                case Opcodes.INVOKESTATIC:
                    System.out.println("INVOKESTATIC");
                    break;
            }
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

    static class FieldByteCodeEnhancer extends FieldVisitor {
        public FieldByteCodeEnhancer() {
            super(Opcodes.ASM5);
        }
    }
}
