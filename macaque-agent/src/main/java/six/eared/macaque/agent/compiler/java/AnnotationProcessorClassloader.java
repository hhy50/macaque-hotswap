package six.eared.macaque.agent.compiler.java;

import java.net.URL;
import java.net.URLClassLoader;

public class AnnotationProcessorClassloader extends URLClassLoader {

    public AnnotationProcessorClassloader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }
}
