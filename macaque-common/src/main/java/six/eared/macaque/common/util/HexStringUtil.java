package six.eared.macaque.common.util;

public class HexStringUtil {

    private static final byte F = (byte) 0xF;

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


    public static String bytes2hexStr(byte[] bytes) {
        char[] chars = new char[bytes.length * 2];

        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i] & 0xFF;
            chars[i*2 + 0] = byteToChar((byte) (b >> 4));
            chars[i*2 + 1] = byteToChar((byte) (b & F));
        }
        return new String(chars);
    }

    private static char byteToChar(byte b) {
        return new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'} [b];
    }

    public static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

//    public static void main(String[] args) {
//        String cafe = "CAFEEFAC";
//        System.out.println(binary2hex(hex2binary(cafe)));
//    }

    public static void main(String[] args) {
        byte[] bytes = FileUtil.readBytes("C:\\Users\\haiyang\\Desktop\\DeviceController.class");
        System.out.println(bytes2hexStr(bytes));
    }
}
