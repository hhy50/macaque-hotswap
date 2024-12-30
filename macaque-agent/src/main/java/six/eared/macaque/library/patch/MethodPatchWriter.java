package six.eared.macaque.library.patch;


import io.github.hhy50.linker.asm.AsmClassBuilder;
import io.github.hhy50.linker.define.MethodDescriptor;
import io.github.hhy50.linker.generate.bytecode.utils.Methods;
import io.github.hhy50.linker.generate.bytecode.vars.ObjectVar;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import six.eared.macaque.agent.accessor.Accessor;
import six.eared.macaque.agent.accessor.AccessorUtil;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.enhance.*;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.common.util.ClassUtil;
import six.eared.macaque.common.util.FileUtil;
import six.eared.macaque.preload.PatchedInvocation;

import java.io.File;

import static org.objectweb.asm.Opcodes.IRETURN;

public class MethodPatchWriter {

    public static MethodVisitor patchMethod(ClassLoader loader, String className, MethodVisitor mv, AsmMethod asmMethod,
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
        invokeDelegationMethod(mv, asmMethod, bindInfo.getBindClass(), accessor.getClassName(), delegationMd);
        return new PatchedMethodUpdater(loader, bindInfo, accessor);
    }

    private static void invokeDelegationMethod(MethodVisitor mv, AsmMethod asmMethod, String patchedClass, String accessor,
                                               MethodDescriptor delegationMd) {
        Type methodType = Type.getMethodType(asmMethod.getDesc());
        mv.visitCode();

        mv.visitTypeInsn(Opcodes.NEW, ClassUtil.className2path(PatchedInvocation.class.getName()));
        mv.visitInsn(Opcodes.DUP);

        // load this
        if (!asmMethod.isStatic()) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
        } else {

        }
        mv.visitLdcInsn(patchedClass);
        mv.visitLdcInsn(accessor);
        Type[] argumentTypes = methodType.getArgumentTypes();
        mv.visitIntInsn(Opcodes.BIPUSH, argumentTypes.length);
        mv.visitTypeInsn(Opcodes.ANEWARRAY, ObjectVar.TYPE.getInternalName());
        for (int i = 0; i < argumentTypes.length; i++) {
            mv.visitInsn(Opcodes.DUP);
            mv.visitIntInsn(Opcodes.BIPUSH, i);
            mv.visitVarInsn(argumentTypes[i].getOpcode(Opcodes.ILOAD), i+(asmMethod.isStatic() ? 0 : 1));
            mv.visitInsn(ObjectVar.TYPE.getOpcode(Opcodes.IASTORE));
        }

        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, ClassUtil.className2path(PatchedInvocation.class.getName()), "<init>",
                "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V", false);

        mv.visitMethodInsn(Opcodes.INVOKESTATIC, delegationMd.getOwner(), delegationMd.getMethodName(), delegationMd.getDesc(), false);
        mv.visitInsn(methodType.getReturnType().getOpcode(IRETURN));
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
}

class PatchedMethodUpdater extends BindMethodWriter {
    private final ClassLoader loader;
    private final MethodBindInfo bindInfo;
    private final AsmClassBuilder bindClassBuilder;

    protected PatchedMethodUpdater(ClassLoader loader, MethodBindInfo bindInfo, Accessor accessor) {
        super(bindInfo, accessor);
        this.loader = loader;
        this.bindClassBuilder = new AsmClassBuilder(Opcodes.ACC_PUBLIC, bindInfo.getBindClass(), Object.class.getName(), null, null)
                .defineConstruct(Opcodes.ACC_PUBLIC)
                .intercept(Methods.invokeSuper().thenReturn());
        this.bindInfo = bindInfo;
    }

    @Override
    public void visitEnd() {
        super.visitEnd();

        this.bindClassBuilder.defineMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                        bindInfo.getBindMethod(), bindInfo.getBindMethodDesc(), null)
                .acceptWithEnd(body -> PatchedMethodUpdater.this.accept(body.getWriter()));
        this.bindClassBuilder.end();
        this.bindInfo.setLoaded(true);

        try {
            byte[] bytecode = this.bindClassBuilder.toBytecode();
            if (Environment.isDebug()) {
                FileUtil.writeBytes(new File(FileUtil.getProcessTmpPath()+"/patched/"+ClassUtil.toSimpleName(bindClassBuilder.getClassName())+".class"),
                        bytecode);
            }
            EnhanceBytecodeClassLoader.loadClass(this.loader, this.bindInfo.getBindClass(), bytecode);
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}