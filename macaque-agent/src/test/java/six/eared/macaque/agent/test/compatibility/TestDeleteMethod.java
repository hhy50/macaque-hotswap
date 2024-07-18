package six.eared.macaque.agent.test.compatibility;

import org.junit.Test;
import six.eared.macaque.agent.hotswap.handler.ClassHotSwapHandler;
import six.eared.macaque.agent.test.Env;
import six.eared.macaque.common.util.FileUtil;
import six.eared.macaque.mbean.rmi.HotSwapRmiData;

public class TestDeleteMethod extends Env {

    private ClassHotSwapHandler classHotSwapHandler = new ClassHotSwapHandler();

    @Test(expected = NoSuchMethodError.class)
    public void testDeleteInstanceMethod() throws Throwable {
        byte[] bytes = compileToClass("TestDeleteMethodClass.java", FileUtil.is2bytes(TestAddMethod.class.getClassLoader()
                .getResourceAsStream("compatibility/delete/DeleteInstanceMethod.java"))).get(0);
        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));
        try {
            Object o = newInstance("six.eared.macaque.agent.test.TestDeleteMethodClass");
            invoke(o, "test2");
        } catch (Exception e) {
            Throwable re = e;
            while (re.getCause() != null) {
                re = re.getCause();
            }
            throw re;
        }
    }

    @Test(expected = NoSuchMethodError.class)
    public void testDeleteStaticMethod() throws Throwable {
        byte[] bytes = compileToClass("TestDeleteMethodClass.java", FileUtil.is2bytes(TestAddMethod.class.getClassLoader()
                .getResourceAsStream("compatibility/delete/DeleteStaticMethod.java"))).get(0);
        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));

        try {
            invokeStatic(getPreload("six.eared.macaque.agent.test.TestDeleteMethodClass"), "test3");
        } catch (Exception e) {
            Throwable re = e;
            while (re.getCause() != null) {
                re = re.getCause();
            }
            throw re;
        }
    }
}
