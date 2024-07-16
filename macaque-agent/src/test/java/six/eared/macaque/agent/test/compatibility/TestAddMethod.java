package six.eared.macaque.agent.test.compatibility;

import org.junit.Assert;
import org.junit.Test;
import six.eared.macaque.agent.hotswap.handler.ClassHotSwapHandler;
import six.eared.macaque.agent.test.Env;
import six.eared.macaque.common.ExtPropertyName;
import six.eared.macaque.common.util.FileUtil;
import six.eared.macaque.mbean.rmi.HotSwapRmiData;

import java.util.HashMap;
import java.util.Map;

public class TestAddMethod extends Env {

    private ClassHotSwapHandler classHotSwapHandler = new ClassHotSwapHandler();

    private Object INSTANCE;

    private Class<?> CLAZZ;

    public TestAddMethod() {
        INSTANCE = newInstance("six.eared.macaque.agent.test.TestAddMethodClass");
        CLAZZ = getPreload("six.eared.macaque.agent.test.TestAddMethodClass");
    }

    @Test
    public void testAddInstanceMethod() {
        byte[] bytes = compileToClass("TestAddMethodClass.java", FileUtil.is2bytes(TestAddMethod.class.getClassLoader()
                .getResourceAsStream("compatibility/add/AddNewInstanceMethod.java"))).get(0);

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals(invoke(INSTANCE, "test2"), "test3");

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals(invoke(INSTANCE, "test2"), "test3");
    }

    @Test
    public void testAddInstanceMethodWithParams() {
        byte[] bytes = compileToClass("TestAddMethodClass.java", FileUtil.is2bytes(TestAddMethod.class.getClassLoader()
                .getResourceAsStream("compatibility/add/AddNewInstanceMethodWithParams.java"))).get(0);

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals(invoke(INSTANCE, "test2"), "arg1=1,arg2=2");

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals(invoke(INSTANCE, "test2"), "arg1=1,arg2=2");
    }

    @Test
    public void testAddStaticMethod() {
        byte[] bytes = compileToClass("TestAddMethodClass.java", FileUtil.is2bytes(TestAddMethod.class.getClassLoader()
                .getResourceAsStream("compatibility/add/AddNewStaticMethod.java"))).get(0);

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals(invoke(INSTANCE, "test2"), "_newStaticMethod");

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals(invoke(INSTANCE, "test2"), "_newStaticMethod");
    }

    @Test
    public void testAddNewStaticMethodWithParams() {
        byte[] bytes = compileToClass("TestAddMethodClass.java", FileUtil.is2bytes(TestAddMethod.class.getClassLoader()
                .getResourceAsStream("compatibility/add/AddNewStaticMethodWithParams.java"))).get(0);
        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));

        Object o = newInstance("six.eared.macaque.agent.test.TestAddMethodClass");
        Assert.assertEquals(invoke(o, "test1"), "arg1=1111,arg2=2222");

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals(invoke(o, "test1"), "arg1=1111,arg2=2222");
    }


//
//    @Test
//    public void testInstanceMethodCovertStatic() {
//        byte[] bytes = compileToClass("EarlyClass2.java", FileUtil.is2bytes(CompatibilityModeTest.class.getClassLoader()
//                .getResourceAsStream("InstanceMethodCovertStatic.java"))).get(0);
//
//        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
//        Assert.assertEquals(earlyClass2.test2(), "testAAA");
//    }
//
//    @Test
//    public void testStaticMethodCovertInstance1() {
//        byte[] bytes = compileToClass("StaticEarlyClass.java", FileUtil.is2bytes(CompatibilityModeTest.class.getClassLoader()
//                .getResourceAsStream("StaticMethodCovertInstance1.java"))).get(0);
//
//        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
//        Assert.assertEquals(staticEarlyClass.test2(), "123");
//    }
//
//    @Test
//    public void testStaticMethodCovertInstance2() {
//        byte[] bytes = compileToClass("StaticEarlyClass.java", FileUtil.is2bytes(CompatibilityModeTest.class.getClassLoader()
//                .getResourceAsStream("StaticMethodCovertInstance2.java"))).get(0);
//
//        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
//        Assert.assertEquals(staticEarlyClass.test2(), "1234");
//    }


//    @Test
//    public void testAddNewMethodWithParams() {
//        byte[] bytes = compileToClass("EarlyClass2.java", FileUtil.is2bytes(CompatibilityModeTest.class.getClassLoader()
//                .getResourceAsStream("AddNewInstanceMethodWithParams.java"))).get(0);
//        ClassHotSwapHandler classHotSwapHandler = new ClassHotSwapHandler();
//        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
//        Assert.assertEquals(earlyClass.test2(), "_newMethod");
//    }


//
//
//    @Test
//    public void testAddNewSimpleMethod2() throws IOException {
//        byte[] bytes = compileToClass("EarlyClass.java", FileUtil.is2bytes(CompatibilityModeTest.class.getClassLoader()
//                .getResourceAsStream("AddNewSimpleMethod2.java"))).get(0);
//        ClassHotSwapHandler classHotSwapHandler = new ClassHotSwapHandler();
//        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
//        Assert.assertEquals(earlyClass.test2(), "_newMethod");
//    }
//
//
//    @Test
//    public void testAddNewStaticMethod() {
//        byte[] bytes = compileToClass("EarlyClass.java", FileUtil.is2bytes(CompatibilityModeTest.class.getClassLoader()
//                .getResourceAsStream("AddNewStaticMethod.java"))).get(0);
//        ClassHotSwapHandler classHotSwapHandler = new ClassHotSwapHandler();
//        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
//        Assert.assertEquals(earlyClass.test3(), "test4");
//    }
//
//    @Test
//    public void testInstanceMethod() {
//        byte[] bytes = compileToClass("EarlyClass.java", FileUtil.is2bytes(CompatibilityModeTest.class.getClassLoader()
//                .getResourceAsStream("AddNewInstanceMethod.java"))).get(0);
//        ClassHotSwapHandler classHotSwapHandler = new ClassHotSwapHandler();
//        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
//        Assert.assertEquals(earlyClass.test3(), "test4");
//    }

    public Map<String, String> compatibilityMode() {
        Map<String, String> extProperties = new HashMap<>();
        extProperties.put(ExtPropertyName.COMPATIBILITY_MODE, Boolean.TRUE.toString());
        return extProperties;
    }
}
