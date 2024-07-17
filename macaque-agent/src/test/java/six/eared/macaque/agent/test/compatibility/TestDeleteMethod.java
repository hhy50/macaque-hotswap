package six.eared.macaque.agent.test.compatibility;

import org.junit.Test;
import six.eared.macaque.agent.hotswap.handler.ClassHotSwapHandler;
import six.eared.macaque.agent.test.Env;
import six.eared.macaque.common.util.FileUtil;
import six.eared.macaque.mbean.rmi.HotSwapRmiData;

public class TestDeleteMethod extends Env {

    private ClassHotSwapHandler classHotSwapHandler = new ClassHotSwapHandler();

    @Test(expected = NoSuchMethodError.class)
    public void testDeleteInstanceMethod() {
        byte[] bytes = compileToClass("TestDeleteMethodClass.java", FileUtil.is2bytes(TestAddMethod.class.getClassLoader()
                .getResourceAsStream("compatibility/delete/DeleteInstanceMethod.java"))).get(0);
        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));

        Object o = newInstance("six.eared.macaque.agent.test.TestAddMethodClass");
        invoke(o, "test2");
    }

    @Test(expected = NoSuchMethodError.class)
    public void testDeleteStaticMethod() {
        byte[] bytes = compileToClass("TestDeleteMethodClass.java", FileUtil.is2bytes(TestAddMethod.class.getClassLoader()
                .getResourceAsStream("compatibility/delete/DeleteStaticMethod.java"))).get(0);
        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));

//        Object o = newInstance("six.eared.macaque.agent.test.TestAddMethodClass");
        invokeStatic(getPreload("six.eared.macaque.agent.test.TestAddMethodClass"), "test3");
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
}
