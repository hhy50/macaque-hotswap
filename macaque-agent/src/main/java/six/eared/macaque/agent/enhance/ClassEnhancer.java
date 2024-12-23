package six.eared.macaque.agent.enhance;

import java.util.HashSet;
import java.util.Set;

public class ClassEnhancer {

    private static final Set<Class<?>> ENHANCED_CLASSES = new HashSet<>();

    /**
     * @param clazz
     */
    public static void enhance(Class<? extends ClassLoader> clazz) {
        ENHANCED_CLASSES.add(clazz);
    }
}
