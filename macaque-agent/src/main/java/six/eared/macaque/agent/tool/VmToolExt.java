package six.eared.macaque.agent.tool;

import arthas.VmTool;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.common.util.InstrumentationUtil;

import java.io.File;
import java.util.Arrays;
import java.util.Set;

public class VmToolExt {

    private static final VmTool TOOL;

    static {
        String libPath = System.getProperty("macaque.arthas.lib");
        if (libPath == null) {
            libPath = System.getenv("macaque.arthas.lib");
        }
        libPath = "C:\\Users\\49168\\IdeaProjects\\macaque-hotswap\\macaque-agent\\src\\main\\resources\\arthas";
        if (libPath != null) {
            String osName = System.getProperty("os.name").toLowerCase();
            String libName = "libArthasJniLibrary-x64.dll";
            if (osName.startsWith("linux")) {
                libName = "libArthasJniLibrary-x64.so";
            } else if (osName.startsWith("mac") || osName.startsWith("darwin")) {
                libName = "libArthasJniLibrary.dylib";
            }
            libPath += File.separator+libName;
        }
        TOOL = VmTool.getInstance(libPath);
    }

    public static Object[] getInstanceByName(String className) {
        Set<Class<?>> loadedClass = InstrumentationUtil.findLoadedClass(Environment.getInst(), className);
        return loadedClass.stream()
                .map(TOOL::getInstances)
                .flatMap(Arrays::stream).toArray();
    }
}

