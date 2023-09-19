package six.eared.macaque.common.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

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

    public static <T> T newInstance(Class<T> clazz, Object... args) {
        try {
            T t = null;
            if (args.length == 0) {
                t = clazz.newInstance();
            } else {
                // TODO 多态类型的构造参数
                Class[] argsType = new Class[args.length];
                for (int i = 0; i < args.length; i++) {
                    argsType[i] = args[i].getClass();
                }
                Constructor<T> constructor = clazz.getDeclaredConstructor(argsType);
                t = constructor.newInstance(args);
            }
            return t;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * TODO  value对应的类型为基本数据类型, 需要调用对应的set方法
     *
     * @param obj
     * @param fieldName
     * @param value
     * @param <T>
     */
    public static <T> void setFieldValue(T obj, String fieldName, Object value) {
        Field[] fields = getFields(obj.getClass());
        for (Field field : fields) {
            if (field.getName().equals(fieldName)) {
                setFieldValue(obj, field, value);
            }
        }
    }

    public static Object getFieldValue(Object obj, Field field) {
        try {
            field.setAccessible(true);
            return field.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> void setFieldValue(T obj, Field field, Object value) {
        try {
            field.setAccessible(true);
            if (value.getClass() == String.class && field.getType() == Integer.class) {
                value = Integer.parseInt((String) value);
            }
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Field[] getFields(Class type) {
        return merge(type.getDeclaredFields(),
                type.getSuperclass().getDeclaredFields(), Field.class);
    }

    public static <T> T[] merge(T[] arr1, T[] arr2, Class<Field> typed) {
        T[] result = (T[]) Array.newInstance(typed, arr1.length + arr2.length);
        int t = 0;
        for (int i = 0; i < arr1.length; i++) {
            result[t++] = arr1[i];
        }
        for (int i = 0; i < arr2.length; i++) {
            result[t++] = arr2[i];
        }
        return result;
    }

    public static boolean hasField(Class<?> clazz, String fieldName) {
        Field[] fields = getFields(clazz);
        for (Field field : fields) {
            if (field.getName().equals(fieldName)) {
                return true;
            }
        }
        return false;
    }

    public static <T extends Annotation> Method findMethodWithAnnotation(Class<?> clazz , Class<T> annotationClass) {
        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            T declaredAnnotation = declaredMethod.getDeclaredAnnotation(annotationClass);
            if (declaredAnnotation != null) {
                return declaredMethod;
            }
        }
        return null;
    }
}