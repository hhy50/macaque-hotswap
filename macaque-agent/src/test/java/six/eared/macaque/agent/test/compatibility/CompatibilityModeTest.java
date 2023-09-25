package six.eared.macaque.agent.test.compatibility;

import org.junit.Assert;
import org.junit.Test;
import six.eared.macaque.agent.compiler.java.JavaSourceCompiler;
import six.eared.macaque.agent.hotswap.handler.ClassHotSwapHandler;
import six.eared.macaque.agent.test.Env;
import six.eared.macaque.agent.test.asm.AsmMethodPrinter;
import six.eared.macaque.agent.test.asm.BinaryClassPrint;
import six.eared.macaque.asm.ClassVisitor;
import six.eared.macaque.common.ExtPropertyName;
import six.eared.macaque.common.util.FileUtil;
import six.eared.macaque.mbean.rmi.HotSwapRmiData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompatibilityModeTest extends Env {

    private ClassVisitor printer = new BinaryClassPrint(new AsmMethodPrinter());

    private EarlyClass earlyClass = new EarlyClass();

    @Test
    public void testSimpleNewMethod() {
        byte[] bytes = compileToClass("EarlyClass.java", FileUtil.is2bytes(CompatibilityModeTest.class.getClassLoader()
                .getResourceAsStream("AddNewSimpleMethod.java")));
        ClassHotSwapHandler classHotSwapHandler = new ClassHotSwapHandler();
        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals(earlyClass.test3(), "test4");
    }

    @Test
    public void testInstanceMethod() {
        byte[] bytes = compileToClass("EarlyClass.java", FileUtil.is2bytes(CompatibilityModeTest.class.getClassLoader()
                .getResourceAsStream("AddNewInstanceMethod.java")));
        ClassHotSwapHandler classHotSwapHandler = new ClassHotSwapHandler();
        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals(earlyClass.test3(), "test4");
    }

    public byte[] compileToClass(String javaFileName, byte[] sourceCode) {
        JavaSourceCompiler javaSourceCompiler = new JavaSourceCompiler();
        Map<String, byte[]> javaSource = new HashMap<>();
        javaSource.put(javaFileName, sourceCode);

        List<byte[]> compiled = javaSourceCompiler.compile(javaSource);
        return compiled.get(0);
    }

    public Map<String, String> compatibilityMode() {
        Map<String, String> extProperties = new HashMap<>();
        extProperties.put(ExtPropertyName.COMPATIBILITY_MODE, Boolean.TRUE.toString());
        return extProperties;
    }
}
