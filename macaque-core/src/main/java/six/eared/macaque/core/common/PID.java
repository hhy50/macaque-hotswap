package six.eared.macaque.core.common;

import java.lang.management.ManagementFactory;
import java.util.function.Supplier;

public class PID {

    public static Integer getCurrentPid() {
        Supplier<Integer> PID = () -> {
            String currentJVM = ManagementFactory.getRuntimeMXBean().getName();
            try {
                return Integer.parseInt(currentJVM.substring(0, currentJVM.indexOf('@')));
            } catch (Exception e) {
                return -1;
            }
        };
        return PID.get();
    }
}
