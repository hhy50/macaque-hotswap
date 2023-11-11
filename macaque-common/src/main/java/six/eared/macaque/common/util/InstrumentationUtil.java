package six.eared.macaque.common.util;

import java.lang.instrument.Instrumentation;
import java.util.HashSet;
import java.util.Set;

public class InstrumentationUtil {

    /**
     *
     * @param inst
     * @param className
     * @return
     */
    public static Set<Class<?>> findLoadedClass(Instrumentation inst, String className) {
        Set<Class<?>> classSet = new HashSet<>();

        for (Class<?> clazz : inst.getAllLoadedClasses()) {
            if (clazz.getName().equals(className)) {
                classSet.add(clazz);
            }
        }
        return classSet;
    }
}
