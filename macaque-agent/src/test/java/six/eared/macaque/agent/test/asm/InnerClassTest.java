package six.eared.macaque.agent.test.asm;


import org.junit.Assert;
import org.junit.Test;
import six.eared.macaque.agent.asm2.enhance.CompatibilityModeClassLoader;
import six.eared.macaque.agent.test.EarlyClass;
import six.eared.macaque.agent.test.compatibility.CompatibilityModeTest;
import six.eared.macaque.asm.ClassWriter;
import six.eared.macaque.asm.MethodVisitor;
import six.eared.macaque.asm.Opcodes;
import six.eared.macaque.common.util.FileUtil;
import six.eared.macaque.common.util.ReflectUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static six.eared.macaque.agent.test.Utils.compileToClass;
import static six.eared.macaque.agent.test.Utils.printClassByteCode;


public class InnerClassTest {

    @Test
    public void testReadInnerClass() {
        List<byte[]> compileds = compileToClass("EarlyClass.java", FileUtil.is2bytes(CompatibilityModeTest.class.getClassLoader()
                .getResourceAsStream("InnerClass.java")));
        printClassByteCode(compileds.get(1));
    }

    @Test
    public void testGenerateInnerClass() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String superClass = "Lsix/eared/macaque/agent/test/EarlyClass;";
        ClassWriter innerCw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        innerCw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "six/eared/macaque/agent/test/EarlyClass$Macaque_Accessor",
                null, "java/lang/Object", null);
        innerCw.visitField(Opcodes.ACC_SYNTHETIC | Opcodes.ACC_FINAL, "this$0", superClass, null, null);

        MethodVisitor constructVisitor = innerCw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(" + superClass + ")V",
                null, null);
        constructVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        constructVisitor.visitVarInsn(Opcodes.ALOAD, 1);
        constructVisitor.visitFieldInsn(Opcodes.PUTFIELD, "six/eared/macaque/agent/test/EarlyClass$Macaque_Accessor", "this$0", superClass);
        constructVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        constructVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        constructVisitor.visitInsn(Opcodes.RETURN);
        constructVisitor.visitMaxs(2, 2);
        constructVisitor.visitEnd();

        for (Method method : ReflectUtil.getDeclaredMethods(EarlyClass.class)) {
            MethodVisitor methodVisitor = innerCw.visitMethod(Opcodes.ACC_PUBLIC, method.getName(), "()Ljava/lang/String;", null, null);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            methodVisitor.visitFieldInsn(Opcodes.GETFIELD, "six/eared/macaque/agent/test/EarlyClass$Macaque_Accessor", "this$0", "Lsix/eared/macaque/agent/test/EarlyClass;");
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "six/eared/macaque/agent/test/EarlyClass", "test1", "()Ljava/lang/String;", false);
            methodVisitor.visitInsn(Opcodes.ARETURN);
            methodVisitor.visitMaxs(1, 1);
        }

        // 定义内部类的字节码
        byte[] innerBytecode = innerCw.toByteArray();
        printClassByteCode(innerBytecode);
        CompatibilityModeClassLoader.loadClass("six.eared.macaque.agent.test.EarlyClass$Macaque_Accessor", innerBytecode);
        Class<?> innerClass = Class.forName("six.eared.macaque.agent.test.EarlyClass$Macaque_Accessor");
        Constructor<?> constructor = innerClass.getConstructor(EarlyClass.class);
        Object innerObj = constructor.newInstance(new EarlyClass());
        Assert.assertNotNull(innerObj);
        Assert.assertEquals(ReflectUtil.invokeMethod(innerObj, "test1"), "test1");
    }
}
