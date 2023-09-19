package six.eared.macaque.agent.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import six.eared.macaque.agent.asm2.AsmField;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.classes.*;
import six.eared.macaque.agent.asm2.enhance.CompatibilityModeMethodVisitor;
import six.eared.macaque.agent.compiler.java.JavaSourceCompiler;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.agent.test.asm.BinaryClassPrint;
import six.eared.macaque.asm.ClassReader;
import six.eared.macaque.asm.ClassWriter;
import six.eared.macaque.asm.FieldVisitor;
import six.eared.macaque.asm.MethodVisitor;

import java.io.IOException;
import java.util.Arrays;
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
            "import six.eared.macaque.asm.*;\n" +
            "\n" +
            "import static six.eared.macaque.asm.Opcodes.ASM4;\n" +
            "\n" +
            "/**\n" +
            " * 反编译\n" +
            " */\n" +
            "public class BinaryClassReader extends ClassVisitor {\n" +
            "private String test1;\n" +
            "private String test2;\n" +
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
    public void testClassReader() throws IOException {
        BinaryClassPrint cp = new BinaryClassPrint();
        ClassReader cr = new ClassReader("six.eared.macaque.mbean.rmi.RmiData");
        cr.accept(cp, 0);
    }

    @Test
    public void testClassByteAr() {
        JavaSourceCompiler javaSourceCompiler = new JavaSourceCompiler();

        Map<String, byte[]> javaSource = new HashMap<>();
        javaSource.put("Main.java", clazz1.getBytes());
        javaSource.put("BinaryClassReader.java", clazz2.getBytes());

        List<byte[]> compiled = javaSourceCompiler.compile(javaSource);
        MultiClassReader clazzDefinitions = null;
        try {
            System.out.println("-----start test noMethodClass-----");
            clazzDefinitions = new MultiClassReader(merge(compiled), mockNoMethodClassVisit());
            for (ClazzDefinition clazzDefinition : clazzDefinitions) {
                printClass(clazzDefinition.getByteCode());
                Assert.assertEquals(clazzDefinition.getAsmMethods().size(), 0);
            }
        } finally {
            System.out.println("-----end test noMethodClass-----");
        }
        System.out.print("\n");
        try {
            System.out.println("-----start test noField-----");
            clazzDefinitions = new MultiClassReader(merge(compiled), mockNoFieldClassVisit());
            for (ClazzDefinition clazzDefinition : clazzDefinitions) {
                printClass(clazzDefinition.getByteCode());
                Assert.assertEquals(clazzDefinition.getAsmFields().size(), 0);
            }
        } finally {
            System.out.println("-----end test noField-----");
        }
    }

    @Test
    public void testMultiClassReader() {
        JavaSourceCompiler javaSourceCompiler = new JavaSourceCompiler();

        Map<String, byte[]> javaSource = new HashMap<>();
        javaSource.put("Main.java", clazz1.getBytes());
        javaSource.put("BinaryClassReader.java", clazz2.getBytes());

        List<byte[]> compiled = javaSourceCompiler.compile(javaSource);

        byte[] bytes = merge(compiled);
        bytes = merge(Arrays.asList(bytes, bytes, bytes));

        MultiClassReader binaryClassReader = new MultiClassReader(bytes);
        for (ClazzDefinition clazzDefinition : binaryClassReader) {
            System.out.println(clazzDefinition.getClassName());
            ClassReader classReader = new ClassReader(clazzDefinition.getByteCode());
            classReader.accept(new BinaryClassPrint(), 0);
        }
    }

    private byte[] merge(List<byte[]> bytelist) {
        return bytelist.stream().reduce((b1, b2) -> {
            int len = b1.length + b2.length;
            byte[] bytes = new byte[len];

            System.arraycopy(b1, 0, bytes, 0, b1.length);
            System.arraycopy(b2, 0, bytes, b1.length, b2.length);
            return bytes;
        }).get();
    }

    public ClazzDefinitionVisitorFactory mockNoMethodClassVisit() {
        return () -> {
            return new ClazzDefinitionVisitor(new AsmMethodVisitor() {
                @Override
                public MethodVisitor visitMethod(AsmMethod method, ClazzDefinition definition, ClassWriter writer) {
                    return null;
                }
            }, null);
        };
    }
    public ClazzDefinitionVisitorFactory mockNoFieldClassVisit() {
        return () -> {
            return new ClazzDefinitionVisitor(null, new AsmFieldVisitor() {
                @Override
                public FieldVisitor visitField(AsmField field, ClazzDefinition clazzDefinition, ClassWriter writer) {
                    return null;
                }
            });
        };
    }

    public static void printClass(byte[] byteCode) {
        ClassReader classReader = new ClassReader(byteCode);
        classReader.accept(new BinaryClassPrint(), 0);
    }
}
