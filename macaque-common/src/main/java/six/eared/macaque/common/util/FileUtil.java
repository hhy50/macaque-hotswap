package six.eared.macaque.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {


    public static byte[] readBytes(String filepath) {
        File file = new File(filepath);
        if (!file.exists()) {
            return null;
        }
        int len = (int) file.length();
        if (len != file.length()) {
            throw new RuntimeException("file length too long");
        }
        byte[] bytes = new byte[len];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(bytes, 0, len);
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public static byte[] is2bytes(InputStream is) {
        byte[] bytes = null;
        try {
            int len = is.available();
            bytes = new byte[len];
            is.read(bytes, 0, len);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bytes;
    }
}
