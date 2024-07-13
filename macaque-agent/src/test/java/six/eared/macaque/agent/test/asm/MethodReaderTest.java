package six.eared.macaque.agent.test.asm;

import org.junit.Test;
import org.objectweb.asm.ClassReader;

import java.io.IOException;


public class MethodReaderTest {

    @Test
    public void test() throws IOException {
        ClassReader classReader = new ClassReader("six.eared.macaque.agent.test.EarlyClass");
        classReader.accept(new BinaryClassPrint(new AsmMethodPrinter()), 0);
    }

    @Test
    public void test2() throws IOException {
        ClassReader classReader = new ClassReader("six.eared.macaque.agent.test.StaticEarlyClass");
        classReader.accept(new BinaryClassPrint(new AsmMethodPrinter()), 0);
    }

    @Test
    public void test3() throws IOException {
        ClassReader classReader = new ClassReader("six.eared.macaque.agent.test.EarlyClass2");
        classReader.accept(new BinaryClassPrint(new AsmMethodPrinter()), 0);
    }
}
