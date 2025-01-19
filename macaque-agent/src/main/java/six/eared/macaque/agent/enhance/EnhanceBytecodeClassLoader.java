package six.eared.macaque.agent.enhance;


import io.github.hhy50.linker.LinkerFactory;
import io.github.hhy50.linker.syslinker.ClassLoaderLinker;
import lombok.SneakyThrows;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.common.util.ClassUtil;
import six.eared.macaque.common.util.FileUtil;

import java.io.File;

public class EnhanceBytecodeClassLoader {

    @SneakyThrows
    public synchronized static Class<?> loadClass(ClassLoader cl, String className, byte[] bytes) {
        if (Environment.isDebug()) {
            System.out.println("loadClass, classloader: "+cl+", className: "+className);
            FileUtil.writeBytes(
                    new File(FileUtil.getProcessTmpPath()+"/compatibility/"+ClassUtil.toSimpleName(className)+".class"),
                    bytes);
        }
        ClassLoaderLinker clLinker = LinkerFactory.createLinker(ClassLoaderLinker.class, cl);
        return clLinker.defineClass(className, bytes, 0, bytes.length);
    }

    public static ClassLoader getClassLoader() {
        return ClassLoader.getSystemClassLoader();
    }
}
