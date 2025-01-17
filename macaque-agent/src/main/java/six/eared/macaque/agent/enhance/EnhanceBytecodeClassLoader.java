package six.eared.macaque.agent.enhance;


import lombok.SneakyThrows;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.common.util.ClassUtil;
import six.eared.macaque.common.util.FileUtil;
import six.eared.macaque.common.util.ReflectUtil;

import java.io.File;

public class EnhanceBytecodeClassLoader {

    @SneakyThrows
    public synchronized static Class<?> loadClass(ClassLoader cl, String className, byte[] bytes) {
        if (Environment.isDebug()) {
            FileUtil.writeBytes(
                    new File(FileUtil.getProcessTmpPath()+"/compatibility/"+ClassUtil.toSimpleName(className)+".class"),
                    bytes);
        }
        System.out.println("classloader: " + cl + ", className: " + className);
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        return (Class<?>) ReflectUtil.invokeMethod(systemClassLoader, "defineClass", className, bytes, 0, bytes.length);
    }

    public static ClassLoader getClassLoader() {
        return ClassLoader.getSystemClassLoader();
    }
}
