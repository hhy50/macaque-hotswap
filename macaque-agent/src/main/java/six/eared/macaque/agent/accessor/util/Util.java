package six.eared.macaque.agent.accessor.util;


public class Util {

    public static Object packing(Object obj) {
        return obj;
    }

    public static Object packing(byte obj) {
        return Byte.valueOf(obj);
    }

    public static Object packing(short obj) {
        return Short.valueOf(obj);
    }

    public static Object packing(int obj) {
        return Integer.valueOf(obj);
    }

    public static Object packing(long obj) {
        return Long.valueOf(obj);
    }

    public static Object packing(float obj) {
        return Float.valueOf(obj);
    }

    public static Object packing(double obj) {
        return Double.valueOf(obj);
    }

    public static Object packing(char obj) {
        return Character.valueOf(obj);
    }

    public static Object packing(boolean obj) {
        return Boolean.valueOf(obj);
    }

    public static byte unpack_byte(Object o) {
        return ((Byte) o).byteValue();
    }

    public static short unpack_short(Object o) {
        return ((Short) o).shortValue();
    }

    public static int unpack_int(Object o) {
        return ((Integer) o).intValue();
    }

    public static long unpack_long(Object o) {
        return ((Long) o).longValue();
    }

    public static float unpack_float(Object o) {
        return ((Float) o).floatValue();
    }

    public static double unpack_double(Object o) {
        return ((Double) o).doubleValue();
    }

    public static char unpack_char(Object o) {
        return ((Character) o).charValue();
    }

    public static boolean unpack_boolean(Object o) {
        return ((Boolean) o).booleanValue();
    }
}
