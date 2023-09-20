package six.eared.macaque.agent.test.compatibility;

import org.junit.Test;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.asm2.classes.ClazzDefinitionVisitor;
import six.eared.macaque.agent.asm2.enhance.CompatibilityModeMethodVisitor;
import six.eared.macaque.agent.compiler.java.JavaSourceCompiler;
import six.eared.macaque.agent.test.asm.AsmMethodPrinter;
import six.eared.macaque.agent.test.asm.BinaryClassPrint;
import six.eared.macaque.asm.ClassReader;
import six.eared.macaque.asm.ClassVisitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestCompatibilityMode {

    private ClassVisitor printer = new BinaryClassPrint(new AsmMethodPrinter());

    public TestCompatibilityMode() {
        EarlyClass earlyClass = new EarlyClass();
        System.out.println(earlyClass);
    }


    @Test
    public void testNewMethod() {
        String clazz = "package six.eared.macaque.agent.test.compatibility;\n" +
                "public class EarlyClass {\n" +
                "\n" +
                "   public String test1() {\n" +
                "       System.out.println(\"test1\");\n" +
                "       return \"test1\";\n" +
                "   }\n" +
                "\n" +
                "\n" +
                "    public String test2() {\n" +
                "        System.out.println(\"test1\");\n" +
                "        return \"test2\";\n" +
                "    }\n" +
                "\n" +
                "    public String test3() {\n" +
                "        System.out.println(\"test1\");\n" +
                "        return \"test3\";\n" +
                "    }\n" +
                "    public String test4() {\n" +
                "        System.out.println(\"test1\");\n" +
                "        return \"test4\";\n" +
                "    }\n" +
                "}";
        byte[] bytes = compileToClass(clazz);
        ClassReader classReader = new ClassReader(bytes);
        ClazzDefinitionVisitor clazzDefinitionVisitor = new ClazzDefinitionVisitor(
                new CompatibilityModeMethodVisitor(), null);
        classReader.accept(clazzDefinitionVisitor, 0);

        ClazzDefinition definition = clazzDefinitionVisitor.getDefinition();
        System.out.println(definition.getAsmMethods());
    }

    public byte[] compileToClass(String clazz) {
        JavaSourceCompiler javaSourceCompiler = new JavaSourceCompiler();
        Map<String, byte[]> javaSource = new HashMap<>();
        javaSource.put("EarlyClass.java", clazz.getBytes());

        List<byte[]> compiled = javaSourceCompiler.compile(javaSource);
        return compiled.get(0);
    }
}
