package six.eared.macaque.common.util;

public class ClassUtil {
    public static String className2path(String clazz) {
        return clazz.replace('.', '/') + ".class";
    }

    public static String classpath2name(String clazz) {
        return clazz.replace('/', '.');
    }
}
