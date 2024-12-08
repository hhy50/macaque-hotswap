package six.eared.macaque.mybatis.mapping;

import io.github.hhy50.linker.annotations.Target;

@Target.Bind("org.apache.ibatis.builder.xml.XMLMapperBuilder")
public interface XMLMapperBuilder {

    void parse();
}
