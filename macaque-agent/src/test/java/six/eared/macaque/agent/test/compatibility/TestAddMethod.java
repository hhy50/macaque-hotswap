package six.eared.macaque.agent.test.compatibility;

import org.junit.Assert;
import org.junit.Test;
import six.eared.macaque.agent.hotswap.handler.ClassHotSwapHandler;
import six.eared.macaque.agent.test.Env;
import six.eared.macaque.common.util.FileUtil;
import six.eared.macaque.mbean.rmi.HotSwapRmiData;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;

public class TestAddMethod extends Env {

    private ClassHotSwapHandler classHotSwapHandler = new ClassHotSwapHandler();

    private Object INSTANCE;

    private Class<?> CLAZZ;

    public static MethodHandles.Lookup LOOKUP;

    static {

    }

    public TestAddMethod() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        INSTANCE = newInstance("six.eared.macaque.agent.test.TestAddMethodClass");
        CLAZZ = getPreload("six.eared.macaque.agent.test.TestAddMethodClass");
//        Constructor var0 = MethodHandles.Lookup.class.getDeclaredConstructors()[0];
//        var0.setAccessible(true);
//        LOOKUP = (MethodHandles.Lookup)var0.newInstance(CLAZZ);
    }

    @Test
    public void testAddInstanceMethod() throws Throwable {
//        MethodType var0 = MethodType.methodType(String.class, new Class[0]);
//        MethodHandle var1 = LOOKUP.findStatic(CLAZZ, "test3", var0);
//        System.out.println((String) Util.unpack_object(var1.invoke()));

        byte[] bytes = compileToClass("TestAddMethodClass.java", FileUtil.is2bytes(TestAddMethod.class.getClassLoader()
                .getResourceAsStream("compatibility/add/AddInstanceMethod.java"))).get(0);

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals(invoke(INSTANCE, "test2"), "12345678test1test390");

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals(invoke(INSTANCE, "test2"), "12345678test1test390");
    }

    @Test
    public void testAddInstanceMethodWithParams() {
        byte[] bytes = compileToClass("TestAddMethodClass.java", FileUtil.is2bytes(TestAddMethod.class.getClassLoader()
                .getResourceAsStream("compatibility/add/AddInstanceMethodWithParams.java"))).get(0);

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals(invoke(INSTANCE, "test2"), "arg1=1,arg2=2");

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals(invoke(INSTANCE, "test2"), "arg1=1,arg2=2");
    }

    @Test
    public void testAddStaticMethod() {
        byte[] bytes = compileToClass("TestAddMethodClass.java", FileUtil.is2bytes(TestAddMethod.class.getClassLoader()
                .getResourceAsStream("compatibility/add/AddStaticMethod.java"))).get(0);

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals(invoke(INSTANCE, "test2"), "_newStaticMethod");

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals(invoke(INSTANCE, "test2"), "_newStaticMethod");
    }

    @Test
    public void testAddStaticMethodWithParams() {
        byte[] bytes = compileToClass("TestAddMethodClass.java", FileUtil.is2bytes(TestAddMethod.class.getClassLoader()
                .getResourceAsStream("compatibility/add/AddStaticMethodWithParams.java"))).get(0);
        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));

        Object o = newInstance("six.eared.macaque.agent.test.TestAddMethodClass");
        Assert.assertEquals(invoke(o, "test1"), "arg1=1111,arg2=2222");

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals(invoke(o, "test1"), "arg1=1111,arg2=2222");
    }

    @Test
    public void testStaticMethodCovertInstance1() {
        byte[] bytes = compileToClass("TestAddMethodClass2.java", FileUtil.is2bytes(TestAddMethod.class.getClassLoader()
                .getResourceAsStream("compatibility/add/StaticMethodCovertInstance1.java"))).get(0);
        Object o = newInstance("six.eared.macaque.agent.test.TestAddMethodClass2");

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals(invoke(o, "test2"), "123");

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals(invoke(o, "test2"), "123");
    }

    @Test
    public void testStaticMethodCovertInstance2() {
        byte[] bytes = compileToClass("TestAddMethodClass2.java", FileUtil.is2bytes(TestAddMethod.class.getClassLoader()
                .getResourceAsStream("compatibility/add/StaticMethodCovertInstance2.java"))).get(0);
        Object o = newInstance("six.eared.macaque.agent.test.TestAddMethodClass2");

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals(invoke(o, "test2"), "1234");

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals(invoke(o, "test2"), "1234");
    }

    @Test
    public void testInstanceMethodCovertStatic() {
        byte[] bytes = compileToClass("TestAddMethodClass.java", FileUtil.is2bytes(TestAddMethod.class.getClassLoader()
                .getResourceAsStream("compatibility/add/InstanceMethodCovertStatic.java"))).get(0);
        Object o = newInstance("six.eared.macaque.agent.test.TestAddMethodClass");

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals(invoke(o, "test1"), "static test3");

        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        Assert.assertEquals(invoke(o, "test2"), "static test3");
    }
}
