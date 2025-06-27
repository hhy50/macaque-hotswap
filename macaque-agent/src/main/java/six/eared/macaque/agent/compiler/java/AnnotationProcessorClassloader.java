package six.eared.macaque.agent.compiler.java;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Set;

public class AnnotationProcessorClassloader extends URLClassLoader {

    private final Set<ClassLoader> resourcesLoader;

    public AnnotationProcessorClassloader(URL[] urls, ClassLoader parent, Set<ClassLoader> resourcesLoader) {
        super(urls, parent);
        this.resourcesLoader = resourcesLoader;
    }
//
//    @Override
//    protected Class<?> findClass(String name) throws ClassNotFoundException {
//        for (ClassLoader classloader : classloaders) {
//            Class<?> c = classloader.loadClass(name);
//            if (c != null) return c;
//        }
//        return super.findClass(name);
//    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        for (ClassLoader classloader : resourcesLoader) {
            Enumeration<URL> resources = classloader.getResources(name);
            if (resources.hasMoreElements()) {
                return resources;
            }
        }
        return super.findResources(name);
    }
}
