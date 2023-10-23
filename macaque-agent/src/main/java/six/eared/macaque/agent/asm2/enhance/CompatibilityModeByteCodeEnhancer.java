package six.eared.macaque.agent.asm2.enhance;

import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.classes.*;
import six.eared.macaque.agent.exceptions.EnhanceException;
import six.eared.macaque.agent.vcs.VersionChainTool;
import six.eared.macaque.asm.*;
import six.eared.macaque.common.util.ClassUtil;

import java.io.IOException;
import java.util.Optional;

public class CompatibilityModeByteCodeEnhancer {

    public static void enhance(ClazzDefinition definition) throws IOException, ClassNotFoundException {
        // 准备阶段
        prepare(definition);

        // 转换阶段
        bytecodeConvert();
    }

    private static void prepare(ClazzDefinition definition) throws IOException, ClassNotFoundException {
//        ClazzDefinition originDefinition = AsmUtil.readOriginClass(definition.getClassName());
        ClazzDefinition lastClassVersion = VersionChainTool.findLastClassVersion(definition.getClassName(), false);
        if (lastClassVersion == null) {
            lastClassVersion = AsmUtil.readOriginClass(definition.getClassName());
        }
        assert lastClassVersion != null;
        for (AsmMethod asmMethod : definition.getAsmMethods()) {
            AsmMethod method = lastClassVersion.getMethod(asmMethod.getMethodName(), asmMethod.getDesc());

            // 已存在的方法
            if (method != null) {
                if (method.isPrivate()) {
                    MethodBindInfo methodBindInfo = null;
                    if (method.getMethodBindInfo() != null) {
                        methodBindInfo = method.getMethodBindInfo().clone();
                    }
                    asmMethod.setMethodBindInfo(methodBindInfo);
                }
                continue;
            }

            // 新方法

        }
    }

    private static void bytecodeConvert() {

    }

    static class Enhancer extends ClassVisitor {

        private ClazzDefinition newDefinition;

        private ClazzDefinition originDefinition;

        public Enhancer(ClazzDefinition newDefinition) throws IOException, ClassNotFoundException {
            super(Opcodes.ASM5, new ClassWriter(0));
            this.newDefinition = newDefinition;
            this.originDefinition = AsmUtil.readOriginClass(newDefinition.getSuperClassName());
        }

        public byte[] toByteArr() {
            ClassWriter writer = (ClassWriter) this.cv;
            return writer.toByteArray();
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            // 本来就有的方法
            if (originDefinition.hasMethod(name, desc)) {
                MethodVisitor writer = super.visitMethod(access, name, desc, signature, exceptions);
                return new MethodByteCodeEnhancer(writer);
            }
            AsmMethod asmMethod = this.newDefinition.getMethod(name, desc);

            // 上一个版本存在的
            ClazzDefinition definition = VersionChainTool.findLastClassVersion(this.newDefinition.getClassName(), false);
            if (definition != null && definition.getMethod(name, desc) != null) {
                MethodBindInfo methodBindInfo = definition.getMethod(name, desc).getMethodBindInfo();
                Optional<CorrelationClazzDefinition> bindClassOp = definition.getCorrelationClasses().stream()
                        .filter(item -> item.getClazzDefinition().getClassName().equals(methodBindInfo.getBindClass())).findFirst();
                if (bindClassOp.isPresent()) {
                    asmMethod.setMethodBindInfo(methodBindInfo.clone());
                    return new BindMethodCaller(this.newDefinition, asmMethod).createProxyObj();
                }
            }

            // 新方法

            AsmMethod method = definition.getMethod(name, desc);
            if (method != null) {
                if (method.getMethodBindInfo() != null) {
                    updateMethodBody(writer, method);
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
            writer.visitMethodInsn(Opcodes.INVOKESPECIAL, accessorClassName, "<init>", "(" + AsmUtil.toTypeDesc(className) + ")V", false);

            writer.visitMethodInsn(Opcodes.INVOKESTATIC, ClassUtil.simpleClassName2path(bindClass), bindMethod, AsmUtil.addArgsDesc(desc, accessorClassName, true), false);
            writer.visitInsn(Opcodes.ARETURN);
        }
    }
}
