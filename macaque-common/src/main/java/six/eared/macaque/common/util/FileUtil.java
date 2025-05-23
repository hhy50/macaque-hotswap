package six.eared.macaque.common.util;

import six.eared.macaque.common.exceptions.FileIOException;

import java.io.*;

public class FileUtil {

    private static String TMP_DIR = null;

    /**
     * 读取字节 byte[]
     *
     * @param filepath
     * @return
     */
    public static byte[] readFile(String filepath) {
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

    /**
     * 写入字节
     *
     * @param file
     * @param bytes
     */
    public static void writeBytes(File file, byte[] bytes) {
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(bytes, 0, bytes.length);
            outputStream.flush();
        } catch (Exception e) {
            throw new FileIOException(e);
        }
    }


    /**
     * 写入字节
     *
     * @param file
     * @param in
     */
    public static void writeBytes(File file, InputStream in) {
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        try (FileOutputStream out = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            in.close();
        } catch (IOException e) {
            throw new FileIOException(e);
        }
    }

    /**
     * inputStream 转 byte[]
     *
     * @param is
     * @return
     */
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

    /**
     * 创建临时文件
     *
     * @param fileName 文件名
     * @param bytes    文件内容
     * @return
     */
    public static File createTmpFile(String fileName, byte[] bytes) {
        File file = new File(getProcessTmpPath(), fileName);
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

    /**
     * 截取文件名
     *
     * @param fileName
     * @return
     */
    public static String getFileName(String fileName) {
        int index = fileName.lastIndexOf("/");
        if (index != -1) {
            fileName = fileName.substring(index+1);
        }
        index = fileName.lastIndexOf(".");
        if (index != -1) {
            return fileName.substring(0, index);
        }
        return fileName;
    }

    /**
     * 获取当前进程自己的临时目录
     *
     * @return
     */
    public static String getProcessTmpPath() {
        if (TMP_DIR == null) {
            String userDir = System.getProperty("user.dir");
            if (new File(userDir+"/build").exists()) {
                userDir+="/build";
            } else {
                userDir += "/tmp";
                File tmpFile = new File(userDir);
                if (!tmpFile.exists()) {
                    tmpFile.mkdirs();
                }
            }
            TMP_DIR = userDir;
        }
        return TMP_DIR;
    }

    /**
     * 删除目录子文件
     *
     * @param file
     * @throws IOException
     */
    public static void deleteFile(File file) {
        if (!file.isDirectory()) {
            file.delete();
            return;
        }
        File[] list = file.listFiles();
        if (list != null) {
            for (File child : list) {
                deleteFile(child);
            }
        }
    }

    /**
     * 清理临时文件
     */
    public static void cleanTmpFile() {
        String processTmpPath = getProcessTmpPath();
        deleteFile(new File(processTmpPath));
    }
}
