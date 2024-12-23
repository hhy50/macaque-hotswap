package six.eared.macaque.library.patch;


import io.github.hhy50.linker.asm.AsmClassBuilder;
import io.github.hhy50.linker.define.MethodDescriptor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import six.eared.macaque.agent.accessor.Accessor;
import six.eared.macaque.agent.accessor.AccessorUtil;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.enhance.*;
import six.eared.macaque.common.util.ClassUtil;
import six.eared.macaque.common.util.FileUtil;

import java.io.File;

import static org.objectweb.asm.Opcodes.IRETURN;

public class MethodPatchWriter {

    public static MethodVisitor patchMethod(String className, MethodVisitor mv, AsmMethod asmMethod,
                                            MethodDescriptor delegationMd) {
        Accessor accessor = AccessorUtil.createAccessor(className, new AccessorClassNameGenerator(), 1);
        MethodBindInfo bindInfo = MethodBindManager.createPatchedBindInfo(
                className,
                asmMethod,
                accessor.getClassName()
        );
        if (bindInfo.isLoaded()) {
            return null;
        }

        // 生成调用“委托方法”的字节码
        invokeDelegationMethod(mv, asmMethod, delegationMd);
        return new PatchedMethodUpdater(bindInfo, accessor);
    }

    private static void invokeDelegationMethod(MethodVisitor mv, AsmMethod asmMethod, MethodDescriptor delegationMd) {
        Type methodType = Type.getMethodType(asmMethod.getDesc());

        mv.visitCode();
        // load this
        if (!asmMethod.isStatic())
            mv.visitVarInsn(Opcodes.ALOAD, 0);

        AsmUtil.loadArgs(mv, methodType.getArgumentTypes(), asmMethod.isStatic());
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, delegationMd.getOwner(), delegationMd.getMethodName(), delegationMd.getDesc(), false);
        mv.visitInsn(methodType.getReturnType().getOpcode(IRETURN));
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
}

class PatchedMethodUpdater extends BindMethodWriter {

    private final MethodBindInfo bindInfo;
    private final AsmClassBuilder bindClassBuilder;

    protected PatchedMethodUpdater(MethodBindInfo bindInfo, Accessor accessor) {
        super(accessor);
        this.bindClassBuilder = AsmUtil.defineClass(Opcodes.ACC_PUBLIC, bindInfo.getBindClass(), null, null, null);
        this.bindInfo = bindInfo;
    }

    @Override
    public void visitEnd() {
        super.visitEnd();

        this.bindClassBuilder.defineMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                        bindInfo.getBindMethod(), bindInfo.getBindMethodDesc(), null)
                .accept(body -> this.accept(body.getWriter()))
                .end();
        this.bindClassBuilder.end();
        this.bindInfo.setLoaded(true);

        byte[] bytecode = this.bindClassBuilder.toBytecode();
        Class<?> aClass = EnhanceBytecodeClassLoader.loadClass(this.bindInfo.getBindClass(), bytecode);
        FileUtil.writeBytes(new File(FileUtil.getProcessTmpPath()+"/patched/"+ClassUtil.toSimpleName(bindClassBuilder.getClassName())+".class"),
                bytecode);
        try {
            aClass.newInstance();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}