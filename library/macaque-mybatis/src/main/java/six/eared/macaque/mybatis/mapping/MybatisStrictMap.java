package six.eared.macaque.mybatis.mapping;

import io.github.hhy50.linker.annotations.Method;
import io.github.hhy50.linker.annotations.Runtime;


@Runtime
public interface MybatisStrictMap {
    @Method.InvokeSuper
    Object put(Object key, Object val);
}
