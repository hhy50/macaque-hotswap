package six.eared.macaque.agent.accessor.util;


public class Util {

    public static Object wrapping(Object obj) {
        return obj;
    }

    public static Object wrapping(byte obj) {
        return Byte.valueOf(obj);
    }

    public static Object wrapping(short obj) {
        return Short.valueOf(obj);
    }

    public static Object wrapping(int obj) {
        return Integer.valueOf(obj);
    }

    public static Object wrapping(long obj) {
        return Long.valueOf(obj);
    }

    public static Object wrapping(float obj) {
        return Float.valueOf(obj);
    }

    public static Object wrapping(double obj) {
        return Double.valueOf(obj);
    }

    public static Object wrapping(char obj) {
        return Character.valueOf(obj);
    }

    public static Object wrapping(boolean obj) {
        return Boolean.valueOf(obj);
    }

    public static byte wrap_byte(Object o) {
        return ((Byte) o).byteValue();
    }

    public static short wrap_short(Object o) {
        return ((Short) o).shortValue();
    }

    public static int wrap_int(Object o) {
        return ((Integer) o).intValue();
    }

    public static long wrap_long(Object o) {
        return ((Long) o).longValue();
    }

    public static float wrap_float(Object o) {
        return ((Float) o).floatValue();
    }

    public static double wrap_double(Object o) {
        return ((Double) o).doubleValue();
    }

    public static char wrap_char(Object o) {
        return ((Character) o).charValue();
    }

    public static boolean wrap_boolean(Object o) {
        return ((Boolean) o).booleanValue();
    }

    public static Object wrap_object(Object obj) {
        return obj;
    }
}
