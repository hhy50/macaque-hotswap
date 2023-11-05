package six.eared.macaque.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT_THREAD_LOCAL = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };

    public static Date now() {
        return new Date();
    }

    public static String nowString() {
        return DATE_FORMAT_THREAD_LOCAL.get().format(now());
    }
}
