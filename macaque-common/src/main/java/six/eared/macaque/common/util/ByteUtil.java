package six.eared.macaque.common.util;

public class ByteUtil {

    public static byte[] hex2binary(String hexString) {
        char[] hexChars = hexString.toCharArray();
        if (StringUtil.isNotEmpty(hexString)) {
            byte[] bytes = new byte[hexChars.length / 2];

            for (int i = hexChars.length - 1, bi = 0; i >= 0; i -= 2) {
                int i1 = Character.digit(hexChars[i - 1], 16);
                int i2 = Character.digit(hexChars[i - 0], 16);
                bytes[bi++] = (byte) ((i2 << 4) | i1);
            }
            return bytes;
        }
        return new byte[0];
    }


    public static short readShort(int index, byte[] bytes) {
        return (short) (((bytes[index] & 0xFF) << 8) | (bytes[index + 1] & 0xFF));
    }
}
