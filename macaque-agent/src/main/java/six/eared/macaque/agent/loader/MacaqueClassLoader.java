package six.eared.macaque.agent.loader;

import java.net.URL;
import java.net.URLClassLoader;

public class MacaqueClassLoader extends URLClassLoader {

    public MacaqueClassLoader(URL aegntUrl, ClassLoader parent) {
        super(new URL[] {aegntUrl}, parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        final Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass != null) {
            return loadedClass;
        }

        if (name != null && (name.startsWith("sun.") || name.startsWith("java."))) {
            return super.loadClass(name, resolve);
        }
        try {
            Class<?> aClass = findClass(name);
            if (resolve) {
                resolveClass(aClass);
            }
            return aClass;
        } catch (Exception e) {
            // ignore
        }
        return super.loadClass(name, resolve);
    }
}
