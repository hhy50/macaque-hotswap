package six.eared.macaque.mybatis.mapping;

import io.github.hhy50.linker.annotations.Method;
import io.github.hhy50.linker.annotations.Target;
import io.github.hhy50.linker.annotations.Typed;

import java.io.InputStream;

@Target.Bind("org.apache.ibatis.builder.xml.XMLMapperBuilder")
public interface XMLMapperBuilder {

    void parse();

    @Method.Constructor
    XMLMapperBuilder newInstance(InputStream in,
                                 @Typed("org.apache.ibatis.session.Configuration") MybatisConfigure configure,
                                 String resource,
                                 @Typed( "java.util.Map") Object sqlFragments,
                                 String namespace);
}
