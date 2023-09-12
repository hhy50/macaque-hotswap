package six.eared.macaque.server.common;

import six.eared.macaque.common.util.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class Banner {

    public static void print() {
        URL url = Banner.class.getClassLoader().getResource("macaque.txt");
        if (url != null) {
            try (InputStream is = url.openStream()) {
                System.out.println(new String(FileUtil.is2bytes(is)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
