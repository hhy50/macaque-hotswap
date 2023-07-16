package com.hhy.common.util;

import java.io.File;
import java.io.FileInputStream;

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

        }
        return bytes;
    }
 }
