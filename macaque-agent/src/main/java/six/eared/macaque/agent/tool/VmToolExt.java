package six.eared.macaque.agent.tool;

import arthas.VmTool;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.common.util.InstrumentationUtil;

import java.util.Arrays;
import java.util.Set;

public class VmToolExt {

    private static final VmTool TOOL = VmTool.getInstance("/root/code/macaque-hotswap/macaque-agent/src/main/resources/libArthasJniLibrary-x64.so");

    public static Object[] getInstanceByName(String className) {
        Set<Class<?>> loadedClass = InstrumentationUtil.findLoadedClass(Environment.getInst(), className);
        return loadedClass.stream()
                .map(TOOL::getInstances)
                .flatMap(Arrays::stream).toArray();
    }
}
