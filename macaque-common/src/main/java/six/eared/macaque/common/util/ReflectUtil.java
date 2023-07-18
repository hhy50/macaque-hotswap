package six.eared.macaque.common.util;

import java.lang.reflect.Field;

public class ReflectUtil {

    public static Field[] getDeclaredFields(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }

        return clazz.getDeclaredFields();
    }

    public static <T> T createInstance(Class<T> clazz) {
        T t = null;
        try {
            t = clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return t;
    }
}