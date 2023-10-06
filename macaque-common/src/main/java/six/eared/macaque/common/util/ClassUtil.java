package six.eared.macaque.common.util;

public class ClassUtil {
    public static String className2path(String clazz) {
        return clazz.replace('.', '/') + ".class";
    }

    public static String simpleClassName2path(String clazz) {
        return clazz.replace('.', '/');
    }

    public static String classpath2name(String clazz) {
        return clazz.replace('/', '.');
    }

    public static String toSimpleName(String className) {
        return className.substring(className.lastIndexOf(".") + 1);
    }
}
