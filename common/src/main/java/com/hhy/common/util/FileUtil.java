package com.hhy.common.util;

import java.io.File;
import java.io.FileInputStream;

public class FileUtil {


    public static byte[] readBytes(String filepath) {
        File file = new File(filepath);
        if (!file.exists()) {
            return null;
        }
        byte[] bytes = null;
        try (FileInputStream fis = new FileInputStream(file)) {
            bytes = fis.readAllBytes();
        } catch (Exception e) {

        }
        return bytes;
    }
 }
