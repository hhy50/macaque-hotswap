package six.eared.macaque.agent.test.compatibility;

import org.junit.Before;
import org.junit.Test;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.asm2.classes.ClazzDefinitionVisitor;
import six.eared.macaque.agent.asm2.enhance.CompatibilityModeMethodVisitor;
import six.eared.macaque.agent.compiler.java.JavaSourceCompiler;
import six.eared.macaque.agent.hotswap.ClassHotSwapper;
import six.eared.macaque.agent.test.TestEnv;
import six.eared.macaque.agent.test.asm.AsmMethodPrinter;
import six.eared.macaque.agent.test.asm.BinaryClassPrint;
import six.eared.macaque.asm.ClassReader;
import six.eared.macaque.asm.ClassVisitor;

import java.lang.instrument.UnmodifiableClassException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestCompatibilityMode extends TestEnv {

    private ClassVisitor printer = new BinaryClassPrint(new AsmMethodPrinter());

    private EarlyClass earlyClass;

    public TestCompatibilityMode() {
        earlyClass = new EarlyClass();
    }

    @Test
    @Before
    public void testNewMethod() throws UnmodifiableClassException, ClassNotFoundException {
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
                "        System.out.println(\"test2\");\n" +
                "        return \"test2\";\n" +
                "    }\n" +
                "\n" +
                "    public String test3() {\n" +
                "        System.out.println(\"test3\");\n" +
                "        test4();   \n" +
                "        return \"test3\";\n" +
                "    }\n" +
                "    public String test4() {\n" +
                "        System.out.println(\"test4\");\n" +
                "        return \"test4\";\n" +
                "    }\n" +
                "}";
        byte[] bytes = compileToClass(clazz);
        ClassReader classReader = new ClassReader(bytes);
        ClazzDefinitionVisitor clazzDefinitionVisitor = new ClazzDefinitionVisitor(
                new CompatibilityModeMethodVisitor(), null);
        classReader.accept(clazzDefinitionVisitor, 0);
        ClazzDefinition definition = clazzDefinitionVisitor.getDefinition();
        ClassHotSwapper.redefine(definition);
        earlyClass.test3();
    }

    public byte[] compileToClass(String clazz) {
        JavaSourceCompiler javaSourceCompiler = new JavaSourceCompiler();
        Map<String, byte[]> javaSource = new HashMap<>();
        javaSource.put("EarlyClass.java", clazz.getBytes());

        List<byte[]> compiled = javaSourceCompiler.compile(javaSource);
        return compiled.get(0);
    }
}
