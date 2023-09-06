package six.eared.macaque.agent.test.asm;

import org.junit.Test;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.asm.ClassReader;
import six.eared.macaque.asm.ClassVisitor;
import six.eared.macaque.asm.MethodVisitor;
import six.eared.macaque.common.util.FileUtil;

import java.util.ArrayList;
import java.util.List;

import static six.eared.macaque.asm.Opcodes.ASM5;

public class TestMethodReader {


    @Test
    public void test() {
        String classPath = "";
        ClassReader classReader = new ClassReader(FileUtil.readBytes(classPath));

        List<AsmMethod> methods = new ArrayList<>();

        int accept = classReader.accept(new ClassVisitor(ASM5) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                AsmMethod asmMethod = new AsmMethod();
                System.out.println("======" + name + "======");
                methods.add(asmMethod);
                return new AsmMethodReader(asmMethod);
            }
        }, 0);

        System.out.println(methods);
    }


}
