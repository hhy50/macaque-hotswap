package six.eared.macaque.agent.test.compatibility;

import org.junit.Assert;
import org.junit.Test;
import six.eared.macaque.agent.hotswap.handler.ClassHotSwapHandler;
import six.eared.macaque.agent.test.Env;
import six.eared.macaque.common.util.FileUtil;
import six.eared.macaque.mbean.rmi.HotSwapRmiData;

import java.lang.invoke.MethodHandles;

public class TestAddMethod extends Env {

    private ClassHotSwapHandler classHotSwapHandler = new ClassHotSwapHandler();

    private Object INSTANCE;

    private Class<?> CLAZZ;

    public static MethodHandles.Lookup LOOKUP;

    static {

    }

    public TestAddMethod() {
        INSTANCE = newInstance("six.eared.macaque.agent.test.TestAddMethodClass");
        CLAZZ = getPreload("six.eared.macaque.agent.test.TestAddMethodClass");
    }

    @Test
    public void testAddInstanceMethod() throws Throwable {
        byte[] bytes = compileToClass("TestAddMethodClass.java", FileUtil.is2bytes(TestAddMethod.class.getClassLoader()
                .getResourceAsStream("compatibility/add/AddInstanceMethod.java"))).get(0);

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals("12345678test1test390", invoke(INSTANCE, "test2"));

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals("12345678test1test390", invoke(INSTANCE, "test2"));
    }

//    @Test
//    public void testAddInstanceMethod2() throws Throwable {
//        byte[] bytes = compileToClass("TestAddMethodClass2.java", FileUtil.is2bytes(TestAddMethod.class.getClassLoader()
//                .getResourceAsStream("compatibility/add/AddInstanceMethod2.java"))).get(0);
//
//        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
//        Assert.assertEquals(invoke(INSTANCE, "test2"), "12345678test1test390");
//
//        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
//        Assert.assertEquals(invoke(INSTANCE, "test2"), "12345678test1test390");
//    }

    @Test
    public void testAddInstanceMethodWithParams() {
        byte[] bytes = compileToClass("TestAddMethodClass.java", FileUtil.is2bytes(TestAddMethod.class.getClassLoader()
                .getResourceAsStream("compatibility/add/AddInstanceMethodWithParams.java"))).get(0);

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals("arg1=1,arg2=2", invoke(INSTANCE, "test2"));

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals("arg1=1,arg2=2", invoke(INSTANCE, "test2"));
    }

    /**
     * 测试新增简单的静态方法
     */
    @Test
    public void testAddStaticMethod() {
        byte[] bytes = compileToClass("TestAddMethodClass.java", FileUtil.is2bytes(TestAddMethod.class.getClassLoader()
                .getResourceAsStream("compatibility/add/AddStaticMethod.java"))).get(0);

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals("_newStaticMethod", invoke(INSTANCE, "test2"));

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals("_newStaticMethod", invoke(INSTANCE, "test2"));
    }

    /**
     * 测试新增静态方法， 新方法里面调用私有的静态方法
     */
    @Test
    public void testAddStaticMethod2() {
        byte[] bytes = compileToClass("TestAddMethodClass.java", FileUtil.is2bytes(TestAddMethod.class.getClassLoader()
                .getResourceAsStream("compatibility/add/AddStaticMethod2.java"))).get(0);

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals("test3_newStaticMethod", invoke(INSTANCE, "test2"));

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals("test3_newStaticMethod", invoke(INSTANCE, "test2"));
    }

    @Test
    public void testAddStaticMethodWithParams() {
        byte[] bytes = compileToClass("TestAddMethodClass.java", FileUtil.is2bytes(TestAddMethod.class.getClassLoader()
                .getResourceAsStream("compatibility/add/AddStaticMethodWithParams.java"))).get(0);
        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));

        Object o = newInstance("six.eared.macaque.agent.test.TestAddMethodClass");
        Assert.assertEquals("arg1=1111,arg2=2222", invoke(o, "test1"));

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals("arg1=1111,arg2=2222", invoke(o, "test1"));
    }

    @Test
    public void testStaticMethodCovertInstance1() {
        byte[] bytes = compileToClass("TestStaticMethodConvertInstanceMethod.java", FileUtil.is2bytes(TestAddMethod.class.getClassLoader()
                .getResourceAsStream("compatibility/add/StaticMethodCovertInstance1.java"))).get(0);
        Object o = newInstance("six.eared.macaque.agent.test.TestStaticMethodConvertInstanceMethod");

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals("123", invoke(o, "test2"));

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals("123", invoke(o, "test2"));
    }

    @Test
    public void testStaticMethodCovertInstance2() {
        byte[] bytes = compileToClass("TestStaticMethodConvertInstanceMethod.java", FileUtil.is2bytes(TestAddMethod.class.getClassLoader()
                .getResourceAsStream("compatibility/add/StaticMethodCovertInstance2.java"))).get(0);
        Object o = newInstance("six.eared.macaque.agent.test.TestStaticMethodConvertInstanceMethod");

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals("1234", invoke(o, "test2"));

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals("1234", invoke(o, "test2"));
    }

    @Test
    public void testInstanceMethodCovertStatic() {
        byte[] bytes = compileToClass("TestAddMethodClass.java", FileUtil.is2bytes(TestAddMethod.class.getClassLoader()
                .getResourceAsStream("compatibility/add/InstanceMethodCovertStatic.java"))).get(0);
        Object o = newInstance("six.eared.macaque.agent.test.TestAddMethodClass");

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals("static test3", invoke(o, "test1"));

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals("static test3", invoke(o, "test2"));
    }
}
