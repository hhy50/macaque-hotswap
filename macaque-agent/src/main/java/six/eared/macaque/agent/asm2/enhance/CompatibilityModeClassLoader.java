package six.eared.macaque.agent.asm2.enhance;


import six.eared.macaque.common.util.ReflectUtil;

public class CompatibilityModeClassLoader {

    public static void loadClass(String className, byte[] bytes) {
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        ReflectUtil.invokeMethod(systemClassLoader, "defineClass", className, bytes, 0, bytes.length);
    }

}
