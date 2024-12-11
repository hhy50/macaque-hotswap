package six.eared.macaque.agent.test.mybatis;

import org.apache.ibatis.annotations.Param;

public interface UserMapper {

    User getById(@Param("id") Long id);
}
