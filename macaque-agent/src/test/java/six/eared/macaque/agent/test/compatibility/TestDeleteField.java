package six.eared.macaque.agent.test.compatibility;

import org.junit.Test;
import six.eared.macaque.agent.hotswap.handler.ClassHotSwapHandler;
import six.eared.macaque.agent.test.Env;
import six.eared.macaque.common.util.FileUtil;
import six.eared.macaque.mbean.rmi.HotSwapRmiData;

public class TestDeleteField extends Env {

    private ClassHotSwapHandler classHotSwapHandler = new ClassHotSwapHandler();

    @Test
    public void testDeleteField() {
        byte[] bytes = compileToClass("TestDeleteFieldClass.java", FileUtil.is2bytes(TestAddMethod.class.getClassLoader()
                .getResourceAsStream("compatibility/delete/DeleteInstanceField.java"))).get(0);
        classHotSwapHandler.handlerRequest(new HotSwapRmiData("class", bytes, compatibilityMode()));

        Object o = newInstance("six.eared.macaque.agent.test.TestDeleteFieldClass");
        invoke(o, "test2");
    }
}
