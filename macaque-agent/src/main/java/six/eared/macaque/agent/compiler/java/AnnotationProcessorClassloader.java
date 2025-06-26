package six.eared.macaque.agent.compiler.java;

import java.util.Set;

public class AnnotationProcessorClassloader extends ClassLoader {

    private final Set<ClassLoader> classloaders;

    public AnnotationProcessorClassloader(Set<ClassLoader> classloaders) {
        this.classloaders = classloaders;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        for (ClassLoader classloader : classloaders) {
            Class<?> c = classloader.loadClass(name);
            if (c != null) return c;
        }
        return null;
    }
}
