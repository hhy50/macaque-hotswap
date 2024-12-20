package six.eared.macaque.mybatis;

import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.library.annotation.Library;

import java.io.IOException;
import java.lang.instrument.Instrumentation;


@Library(name = "mybatis", hooks = MybatisXmlMapperHandler.class)
public class MybatisLibraryConfiguration {

    public static void init() throws IOException, ClassNotFoundException {
        Instrumentation instrumentation = Environment.getInst();
        instrumentation.addTransformer(new StrictMapTransformer());
    }
}
