package six.eared.macaque.agent.asm2.enhance;


import six.eared.macaque.common.util.ReflectUtil;

import java.util.HashSet;

public class CompatibilityModeClassLoader {

    private static final HashSet<String> NAMESPACE = new HashSet<>();

    public static void loadClass(String className, byte[] bytes) {
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        ReflectUtil.invokeMethod(systemClassLoader, "defineClass", className, bytes, 0, bytes.length);
        NAMESPACE.add(className);
    }

    public static boolean isLoaded(String className) {
        return NAMESPACE.contains(className);
    }
}
