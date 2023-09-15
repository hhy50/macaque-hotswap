package six.eared.macaque.common.util;

import six.eared.macaque.common.exceptions.FileIOException;

import java.io.*;

import static java.io.File.separator;

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
            throw new FileIOException(e);
        }
    }

    public static void writeBytes(File file, byte[] bytes) {
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(bytes, 0, bytes.length);
            outputStream.flush();
        } catch (Exception e) {
            throw new FileIOException(e);
        }
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

    public static File createTmpFile(byte[] bytes) {
        String tmpdir = System.getProperty("java.io.tmpdir");
        if (StringUtil.isEmpty(tmpdir)) {
            tmpdir = System.getProperty("user.home") + separator + "tmp" + separator;
        }
        tmpdir += "macaque" + separator;

        File file = new File(tmpdir, "Main.java");

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try {
            file.createNewFile();
            writeBytes(file, bytes);
        } catch (IOException e) {
            throw new FileIOException(e);
        }
        return file;
    }

    public static File createTmpFile(String fileName, byte[] bytes) {
        String tmpdir = System.getProperty("java.io.tmpdir");
        if (StringUtil.isEmpty(tmpdir)) {
            tmpdir = System.getProperty("user.home") + separator + "tmp" + separator;
        }
        tmpdir += "macaque" + separator;

        File file = new File(tmpdir, fileName);

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try {
            file.createNewFile();
            writeBytes(file, bytes);
        } catch (IOException e) {
            throw new FileIOException(e);
        }
        return file;
    }
}
