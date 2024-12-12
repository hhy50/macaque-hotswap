package six.eared.macaque.agent.test.mybatis;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;
import six.eared.macaque.agent.hotswap.handler.ClassHotSwapHandler;
import six.eared.macaque.common.util.FileUtil;
import six.eared.macaque.mbean.rmi.HotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

public class TestUpdateSqlStatement extends six.eared.macaque.agent.test.Env  {

    private ClassHotSwapHandler classHotSwapHandler = new ClassHotSwapHandler();

    private SqlSessionFactory sqlSessionFactory;

    public TestUpdateSqlStatement() throws IOException {
        try (InputStream is = TestUpdateSqlStatement.class.getClassLoader()
                .getResourceAsStream("mybatis/mybatis-config.xml");) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(is);
        }
    }

    @Test
    public void test1() {
        RmiResult result = classHotSwapHandler.handlerRequest(new HotSwapRmiData("xml",
                FileUtil.is2bytes(TestUpdateSqlStatement.class.getClassLoader().getResourceAsStream("mybatis/UserMapper.xml.1")),
                Collections.emptyMap()));
        if (!result.isSuccess()) {
            throw new RuntimeException(result.getMessage());
        }
    }
}
