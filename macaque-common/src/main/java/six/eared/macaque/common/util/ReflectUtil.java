package six.eared.macaque.common.util;

import java.lang.reflect.Array;
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

    public static <T> T newInstance(Class<T> clazz) {
        try {
            T t = clazz.newInstance();
            return t;
        } catch (Exception e) {

        }
        return null;
    }

    public static <T> void setFieldValue(T obj, String fieldName, Object value) {
        Field[] fields = getFields(obj.getClass());
        for (Field field : fields) {
            if (field.getName().equals(fieldName)) {
                field.setAccessible(true);
                try {
                    if (value.getClass() == String.class && field.getType() == Integer.class) {
                        value = Integer.parseInt((String) value);
                    }
                    field.set(obj, value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Field[] getFields(Class type) {
        return merge(type.getDeclaredFields(),
                type.getSuperclass().getDeclaredFields(), Field.class);
    }

    public static <T> T[] merge(T[] arr1, T[] arr2, Class<Field> fieldClass) {
        T[] result = (T[]) Array.newInstance(fieldClass, arr1.length + arr2.length);
        int t = 0;
        for (int i = 0; i < arr1.length; i++) {
            result[t++] = arr1[i];
        }
        for (int i = 0; i < arr2.length; i++) {
            result[t++] = arr2[i];
        }
        return result;
    }
}