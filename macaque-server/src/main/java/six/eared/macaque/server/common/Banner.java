package six.eared.macaque.server.common;

import six.eared.macaque.common.util.FileUtil;

import java.io.InputStream;

public class Banner {

    public static void print() {
        try (InputStream is = Banner.class.getClassLoader().getResourceAsStream("macaque.txt")) {
            byte[] bytes = FileUtil.is2bytes(is);
            if (bytes != null) {
                System.out.println(new String(bytes));
            }
        } catch (Exception e) {

        }
    }
}
