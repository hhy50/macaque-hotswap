package six.eared.macaque.common.util;

import java.util.List;

public class CollectionUtil {

    public static <T> boolean isNotEmpty(List<T> list) {
        return list != null && list.size() > 0;
    }
}
