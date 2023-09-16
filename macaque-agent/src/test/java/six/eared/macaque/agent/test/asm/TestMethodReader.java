package six.eared.macaque.agent.test.asm;

import org.junit.Test;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.asm.ClassReader;
import six.eared.macaque.asm.ClassVisitor;
import six.eared.macaque.asm.ClassWriter;
import six.eared.macaque.asm.MethodVisitor;
import six.eared.macaque.common.util.FileUtil;

import java.util.ArrayList;
import java.util.List;

import static six.eared.macaque.asm.Opcodes.ASM5;

public class TestMethodReader {


    @Test
    public void test() {
        String classPath = "C:\\Users\\haiyang\\Desktop\\PiDeviceTestController.class";
        ClassReader classReader = new ClassReader(FileUtil.readBytes(classPath));
        ClassWriter classWriter = new ClassWriter(0);
        List<AsmMethod> methods = new ArrayList<>();

        int accept = classReader.accept(new ClassVisitor(ASM5, classWriter) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                MethodVisitor methodWriter = super.visitMethod(access, name, desc, signature, exceptions);
                AsmMethod asmMethod = AsmMethod.AsmMethodBuilder
                        .builder()
                        .modifier(access)
                        .methodName(name)
                        .methodSign(signature)
                        .build();
                methods.add(asmMethod);
                return new MethodVisitor(ASM5, methodWriter) {

                };
            }
        }, 0);
        byte[] byteArray = classWriter.toByteArray();
        System.out.println(new String(byteArray));
    }
}
