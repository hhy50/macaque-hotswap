package six.eared.macaque.agent.test.asm;


import org.junit.Assert;
import org.junit.Test;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.ClassBuilder;
import six.eared.macaque.agent.asm2.enhance.CompatibilityModeClassLoader;
import six.eared.macaque.agent.test.AbsEarlyClass;
import six.eared.macaque.agent.test.EarlyClass;
import six.eared.macaque.agent.test.Env;
import six.eared.macaque.agent.test.compatibility.CompatibilityModeTest;
import six.eared.macaque.asm.ClassWriter;
import six.eared.macaque.asm.Label;
import six.eared.macaque.asm.MethodVisitor;
import six.eared.macaque.asm.Opcodes;
import six.eared.macaque.common.util.ClassUtil;
import six.eared.macaque.common.util.FileUtil;
import six.eared.macaque.common.util.ReflectUtil;

import java.io.IOException;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static six.eared.macaque.agent.test.Utils.compileToClass;
import static six.eared.macaque.agent.test.Utils.printClassByteCode;


public class InnerClassTest extends Env {

    @Test
    public void testReadInnerClass() {
        List<byte[]> compileds = compileToClass("EarlyClass.java", FileUtil.is2bytes(CompatibilityModeTest.class.getClassLoader()
                .getResourceAsStream("InnerClass.java")));
        for (byte[] compiled : compileds) {
            printClassByteCode(compiled);
        }
    }

    @Test
    public void testReadInnerClass2() throws ClassNotFoundException, IOException {
        printClassByteCode(AsmUtil.readOriginClass("six.eared.macaque.agent.test.EarlyClass").getOriginData());
        printClassByteCode(AsmUtil.readOriginClass("six.eared.macaque.agent.test.EarlyClass$Macaque_Accessor").getOriginData());
    }

    @Test
    public void testGenerateInnerClass() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String outClassName = EarlyClass.class.getName();
        String innerClassName = outClassName + "$Macaque_Accessor";
        String outClassDesc = AsmUtil.toTypeDesc(outClassName);

        ClassBuilder classBuilder = AsmUtil.defineClass(Opcodes.ACC_PUBLIC, innerClassName, null, null, null)
                .defineField(Opcodes.ACC_SYNTHETIC | Opcodes.ACC_FINAL, "this$0", outClassDesc, null, null)
                .defineConstruct(Opcodes.ACC_PUBLIC, new String[]{outClassName}, null, null)
                .accept(constructVisitor -> {
                    constructVisitor.visitVarInsn(Opcodes.ALOAD, 0);
                    constructVisitor.visitVarInsn(Opcodes.ALOAD, 1);
                    constructVisitor.visitFieldInsn(Opcodes.PUTFIELD, ClassUtil.simpleClassName2path(innerClassName), "this$0", outClassDesc);
                    constructVisitor.visitVarInsn(Opcodes.ALOAD, 0);
                    constructVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
                    constructVisitor.visitInsn(Opcodes.RETURN);
                    constructVisitor.visitMaxs(2, 2);
                    constructVisitor.visitEnd();
                });

        for (Method method : ReflectUtil.getDeclaredMethods(EarlyClass.class)) {
            classBuilder
                    .defineMethod(Opcodes.ACC_PUBLIC, method.getName(), "()Ljava/lang/String;", null, null)
                    .accept(methodVisitor -> {
                        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
                        methodVisitor.visitFieldInsn(Opcodes.GETFIELD, ClassUtil.simpleClassName2path(innerClassName), "this$0", outClassDesc);
                        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, ClassUtil.simpleClassName2path(outClassName), "test1", "()Ljava/lang/String;", false);
                        methodVisitor.visitInsn(Opcodes.ARETURN);
                        methodVisitor.visitMaxs(1, 1);
                    });
        }
        classBuilder.end();

        // 定义内部类的字节码
        byte[] innerBytecode = classBuilder.toByteArray();
        printClassByteCode(innerBytecode);
        CompatibilityModeClassLoader.loadClass(innerClassName, innerBytecode);
        Class<?> innerClass = Class.forName(innerClassName);
        Constructor<?> constructor = innerClass.getConstructor(EarlyClass.class);
        Object innerObj = constructor.newInstance(new EarlyClass());
        Assert.assertNotNull(innerObj);
        Assert.assertEquals(ReflectUtil.invokeMethod(innerObj, "test1"), "test1");
    }

    @Test
    public void testInnerInvokerSuper() throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException, UnmodifiableClassException, InterruptedException {
        String superClass = "Lsix/eared/macaque/agent/test/EarlyClass;";
        ClassWriter innerCw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        innerCw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "six/eared/macaque/agent/test/EarlyClass_Bind",
                null, "six/eared/macaque/agent/test/AbsEarlyClass", null);

        MethodVisitor constructVisitor = innerCw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V",
                null, null);
        constructVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        constructVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "six/eared/macaque/agent/test/AbsEarlyClass", "<init>", "()V", false);
        constructVisitor.visitInsn(Opcodes.RETURN);
        constructVisitor.visitMaxs(1, 1);
        constructVisitor.visitEnd();

        MethodVisitor methodVisitor1 = innerCw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "access$001",
                "(Lsix/eared/macaque/agent/test/EarlyClass_Bind;)Ljava/lang/String;", null, null);
        methodVisitor1.visitLineNumber(3, new Label());
        methodVisitor1.visitVarInsn(Opcodes.ALOAD, 0);
        methodVisitor1.visitMethodInsn(Opcodes.INVOKESPECIAL, "six/eared/macaque/agent/test/AbsEarlyClass", "test1", "()Ljava/lang/String;", false);
        methodVisitor1.visitInsn(Opcodes.ARETURN);
        methodVisitor1.visitMaxs(1, 1);

        // 定义内部类的字节码
        byte[] innerBytecode = innerCw.toByteArray();
        printClassByteCode(innerBytecode);
        CompatibilityModeClassLoader.loadClass("six.eared.macaque.agent.test.EarlyClass_Bind", innerBytecode);
        Class<?> innerClass = Class.forName("six.eared.macaque.agent.test.EarlyClass_Bind");
        Object o = innerClass.newInstance();
        Method test1 = innerClass.getDeclaredMethod("access$001", innerClass);
        Assert.assertEquals(test1.invoke(null, o), "abs test1");
    }

    @Test
    public void testInnerInvokerSuper2() throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException, UnmodifiableClassException, InterruptedException {
        EarlyClass earlyClass = new EarlyClass();

        MethodType type = MethodType.methodType(String.class);

        MethodHandles.Lookup lookup = ReflectUtil.newInstance(MethodHandles.Lookup.class, earlyClass.getClass());
        try {
            MethodHandle mh = lookup.findSpecial(AbsEarlyClass.class,
                    "test1", type, earlyClass.getClass());
            Object invoke = mh.invoke(earlyClass);
            System.out.println(invoke);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
