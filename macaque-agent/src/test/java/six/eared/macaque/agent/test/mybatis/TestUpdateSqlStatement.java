package six.eared.macaque.agent.test.mybatis;

import org.junit.Test;
import six.eared.macaque.agent.hotswap.handler.ClassHotSwapHandler;
import six.eared.macaque.common.util.FileUtil;
import six.eared.macaque.mbean.rmi.HotSwapRmiData;

import java.util.Collections;

public class TestUpdateSqlStatement extends six.eared.macaque.agent.test.Env  {

    private ClassHotSwapHandler classHotSwapHandler = new ClassHotSwapHandler();

    @Test
    public void test1() {
        classHotSwapHandler.handlerRequest(new HotSwapRmiData("xml",
                FileUtil.readFile("C:\\Users\\49168\\IdeaProjects\\content-center\\content-service\\src\\main\\resources\\mapper\\ContentCommentInfoMapper.xml"),
                Collections.emptyMap()));
    }
}
