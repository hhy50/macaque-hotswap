package six.eared.macaque.agent.asm2.enhance;


import six.eared.macaque.common.util.ClassUtil;
import six.eared.macaque.common.util.FileUtil;
import six.eared.macaque.common.util.ReflectUtil;

import java.io.File;
import java.util.HashSet;

public class CompatibilityModeClassLoader {

    private static final HashSet<String> NAMESPACE = new HashSet<>();

    public synchronized static void loadClass(String className, byte[] bytes) {
        if (!isLoaded(className)) {
            ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
            FileUtil.writeBytes(
                    new File("C:\\Users\\haiyang\\IdeaProjects\\macaque-hotswap\\macaque-agent\\build" + File.separator + ClassUtil.toSimpleName(className) + ".class"),
                    bytes);
            Class<?> clazz = (Class<?>) ReflectUtil.invokeMethod(systemClassLoader, "defineClass", className, bytes, 0, bytes.length);
            NAMESPACE.add(className);
        }
    }

    public static boolean isLoaded(String className) {
        return NAMESPACE.contains(className);
    }
}
