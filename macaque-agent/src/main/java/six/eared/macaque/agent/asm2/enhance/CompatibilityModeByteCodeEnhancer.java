package six.eared.macaque.agent.asm2.enhance;

import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.exceptions.EnhanceException;
import six.eared.macaque.agent.vcs.VersionChainTool;
import six.eared.macaque.asm.*;
import six.eared.macaque.common.util.ClassUtil;

public class CompatibilityModeByteCodeEnhancer {

    public static byte[] enhance(byte[] bytecode) {
        Enhancer enhancer = new Enhancer();
        ClassReader classReader = new ClassReader(bytecode);
        classReader.accept(enhancer, 0);
        return enhancer.toByteArr();
    }

    static class Enhancer extends ClassVisitor {

        private String className;

        public Enhancer() {
            super(Opcodes.ASM5, new ClassWriter(0));
        }

        public byte[] toByteArr() {
            ClassWriter writer = (ClassWriter) this.cv;
            return writer.toByteArray();
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            this.className = ClassUtil.classpath2name(name);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor writer = super.visitMethod(access, name, desc, signature, exceptions);

            String targetClassName = ClassUtil.classpath2name(this.className);
            ClazzDefinition definition = VersionChainTool.findLastClassVersion(targetClassName, true);
            if (definition == null) {
                return new MethodByteCodeEnhancer(writer);
            }

            AsmMethod method = definition.getMethod(name, desc);
            if (method != null) {
                if (method.getMethodBindInfo() != null) {
                    //
//                    updateMethodBody(writer, method, desc);
                    return null;
                }
                return new MethodByteCodeEnhancer(writer);
            }
            throw new EnhanceException("attempted to add a method");
        }

        private void updateMethodBody(MethodVisitor writer, AsmMethod method) {
            MethodBindInfo methodBindInfo = method.getMethodBindInfo();
            String accessorClassName = methodBindInfo.getAccessorClassName();
            String bindClass = methodBindInfo.getBindClass();
            String bindMethod = methodBindInfo.getBindMethod();
            String desc = method.getDesc();

            writer.visitTypeInsn(Opcodes.NEW, accessorClassName);
            writer.visitInsn(Opcodes.DUP);
            writer.visitVarInsn(Opcodes.ALOAD, 0);
            writer.visitMethodInsn(Opcodes.INVOKESPECIAL, accessorClassName, "<init>", "("+ AsmUtil.toTypeDesc(className) +")V", false);

            writer.visitMethodInsn(Opcodes.INVOKESTATIC, ClassUtil.simpleClassName2path(bindClass), bindMethod, AsmUtil.addArgsDesc(desc, accessorClassName, true), false);
            writer.visitInsn(Opcodes.ARETURN);
        }
    }

    static class MethodByteCodeEnhancer extends MethodVisitor {

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
}
