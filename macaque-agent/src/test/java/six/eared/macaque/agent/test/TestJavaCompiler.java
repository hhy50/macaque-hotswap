package six.eared.macaque.agent.test;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import six.eared.macaque.agent.asm.classes.BinaryClassReader;
import six.eared.macaque.agent.compiler.java.JavaSourceCompiler;
import six.eared.macaque.agent.env.Environment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestJavaCompiler {

    static String clazz1 =
            "public class Main {\n" +
            "    public static void main(String[] args) throws InterruptedException {\n" +
            "        User user = new User(\"1123\", 111);\n" +
            "        System.out.println(user);\n" +
            "\n" +
            "        while (true) {\n" +
            "            try {\n" +
            "                test();\n" +
            "                Thread.sleep(1000);\n" +
            "            } catch (InterruptedException e) {\n" +
            "                throw new RuntimeException(e);\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "    public static void test() {\n" +
            "        System.out.println(111111);\n" +
            "    }\n" +
            "    static class User {\n" +
            "        private String name;\n" +
            "\n" +
            "        private Integer age;\n" +
            "\n" +
            "        User(String name, Integer age) {\n" +
            "            this.name = name;\n" +
            "            this.age = age;\n" +
            "        }\n" +
            "\n" +
            "\n" +
            "        @Override\n" +
            "        public String toString() {\n" +
            "            return \"User{\" +\n" +
            "                    \"name='\" + name + '\\'' +\n" +
            "                    \", age=\" + age +\n" +
            "                    '}';\n" +
            "        }\n" +
            "    }\n" +
            "}\n";

    static String clazz2 = "package six.eared.macaque.agent.asm.classes;\n" +
            "\n" +
            "import org.objectweb.asm.*;\n" +
            "\n" +
            "import static org.objectweb.asm.Opcodes.ASM4;\n" +
            "\n" +
            "/**\n" +
            " * 反编译\n" +
            " */\n" +
            "public class BinaryClassReader extends ClassVisitor {\n" +
            "    public BinaryClassReader() {\n" +
            "        super(ASM4);\n" +
            "    }\n" +
            "\n" +
            "    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {\n" +
            "        System.out.println(name + \" extends \" + superName + \" {\");\n" +
            "    }\n" +
            "\n" +
            "    public void visitSource(String source, String debug) {\n" +
            "    }\n" +
            "\n" +
            "    public void visitOuterClass(String owner, String name, String desc) {\n" +
            "    }\n" +
            "\n" +
            "    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {\n" +
            "        return null;\n" +
            "    }\n" +
            "\n" +
            "    public void visitAttribute(Attribute attr) {\n" +
            "    }\n" +
            "\n" +
            "    public void visitInnerClass(String name, String outerName, String innerName, int access) {\n" +
            "    }\n" +
            "\n" +
            "    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {\n" +
            "        System.out.println(\"    \" + desc + \" \" + name);\n" +
            "        return null;\n" +
            "    }\n" +
            "\n" +
            "    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {\n" +
            "        System.out.println(\"    \" + name + desc);\n" +
            "        return null;\n" +
            "    }\n" +
            "\n" +
            "    public void visitEnd() {\n" +
            "        System.out.println(\"}\");\n" +
            "    }\n" +
            "}\n";

    @Before
    public void before() {
        Environment.initEnv(true, null);
    }

    @Test
    public void testClassByteAr () {
        JavaSourceCompiler javaSourceCompiler = new JavaSourceCompiler();

        Map<String, String> javaSource = new HashMap<>();
        javaSource.put("Main.java", clazz1);
        javaSource.put("BinaryClassReader.java", clazz2);

        List<byte[]> compiled = javaSourceCompiler.compile(javaSource);
        BinaryClassReader binaryClassReader = new BinaryClassReader();
        for (byte[] bytes : compiled) {
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(binaryClassReader, 0);
        }
    }

}
