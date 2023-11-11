package six.eared.macaque.agent.test;

import six.eared.macaque.agent.compiler.java.JavaSourceCompiler;
import six.eared.macaque.agent.test.asm.AsmMethodPrinter;
import six.eared.macaque.agent.test.asm.BinaryClassPrint;
import six.eared.macaque.asm.ClassReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    public static List<byte[]> compileToClass(String javaFileName, byte[] sourceCode) {
        JavaSourceCompiler javaSourceCompiler = JavaSourceCompiler.getInstance();
        Map<String, byte[]> javaSource = new HashMap<>();
        javaSource.put(javaFileName, sourceCode);

        return javaSourceCompiler.compile(javaSource);
    }

    public static void printClassByteCode(byte[] bytes) {
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(new BinaryClassPrint(new AsmMethodPrinter("        ")), 0);
    }
}
