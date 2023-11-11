package six.eared.macaque.common.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;

public class ReflectUtil {

    public static Field[] getDeclaredFields(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }

        return clazz.getDeclaredFields();
    }

    public static Method[] getDeclaredMethods(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }

        return clazz.getDeclaredMethods();
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

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<T> clazz, Object... args) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        if (args.length == 0) {
            return clazz.newInstance();
        } else {
            Class<?>[] paramTypes = new Class<?>[args.length];
            for (int i = 0; i < paramTypes.length; i++) {
                paramTypes[i] = args[i].getClass();
            }
            for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                if (checkParamsType(constructor.getParameterTypes(), paramTypes)) {
                    constructor.setAccessible(true);
                    return (T) constructor.newInstance(args);
                }
            }
            throw new IllegalStateException("Not find Constructor with type: " + Arrays.toString(paramTypes));
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

    public static <T extends Annotation> Method findMethodWithAnnotation(Class<?> clazz, Class<T> annotationClass) {
        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            T declaredAnnotation = declaredMethod.getDeclaredAnnotation(annotationClass);
            if (declaredAnnotation != null) {
                return declaredMethod;
            }
        }
        return null;
    }

    public static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        Class<?> searchType = clazz;
        while (searchType != null) {
            Method[] methods = (searchType.isInterface() ? searchType.getMethods() : searchType.getDeclaredMethods());
            for (Method method : methods) {
                if (name.equals(method.getName()) &&
                        (paramTypes == null || checkParamsType(method.getParameterTypes(), paramTypes))) {
                    return method;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

    private static boolean checkParamsType(Class<?>[] requiredTypes, Class<?>[] paramTypes) {
        if (requiredTypes.length != paramTypes.length) {
            return false;
        }
        if (requiredTypes.length == 0) {
            return true;
        }
        int len = requiredTypes.length;

        for (int i = 0; i < len; i++) {
            if (!Object.class.isAssignableFrom(requiredTypes[i])) {
                requiredTypes[i] = basicType2ObjectType(requiredTypes[i]);
            }
            if (!Object.class.isAssignableFrom(paramTypes[i])) {
                paramTypes[i] = basicType2ObjectType(paramTypes[i]);
            }
            if (!requiredTypes[i].isAssignableFrom(paramTypes[i])) {
                return false;
            }
        }
        return true;
    }

    private static Class<?> basicType2ObjectType(Class<?> type) {
        switch (type.getName()) {
            case "int":
                return Integer.class;
        }
        throw new ClassCastException();
    }

    public static Object invokeMethod(Object target, String methodName, Object... args) {
        try {
            Class<?>[] paramTypes = new Class<?>[args.length];
            for (int i = 0; i < paramTypes.length; i++) {
                paramTypes[i] = args[i].getClass();
            }

            Method method = findMethod(target.getClass(), methodName, paramTypes);
            if (method == null) {
                throw new NoSuchMethodException();
            }
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }


    public static Object invokeStaticMethod(Class<?> target, String methodName, Object... args) {
        try {
            Class<?>[] paramTypes = new Class<?>[args.length];
            for (int i = 0; i < paramTypes.length; i++) {
                paramTypes[i] = args[i].getClass();
            }

            Method method = findMethod(target, methodName, paramTypes);
            if (method == null) {
                throw new NoSuchMethodException();
            }
            method.setAccessible(true);
            return method.invoke(null, args);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}