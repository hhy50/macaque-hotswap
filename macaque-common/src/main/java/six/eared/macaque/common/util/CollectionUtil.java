package six.eared.macaque.common.util;

import java.util.List;
import java.util.Map;

public class CollectionUtil {

    public static <T> boolean isNotEmpty(List<T> list) {
        return list != null && list.size() > 0;
    }

    public static boolean isNotEmpty(Map<String, String> map) {
        return map != null && !map.isEmpty();
    }
}
