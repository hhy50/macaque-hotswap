package six.eared.macaque.agent.env;

import java.lang.instrument.Instrumentation;
import java.util.concurrent.atomic.AtomicBoolean;

public class Environment {

    private static final AtomicBoolean INIT_FLAG = new AtomicBoolean(false);
    private static boolean DEBUG = false;
    public static Instrumentation INST = null;

    public synchronized static void initEnv(boolean debug, Instrumentation inst) {
        if (INIT_FLAG.compareAndSet(false, true)) {
            Environment.DEBUG = debug;
            Environment.INST = inst;
        }
    }

    public static boolean isDebug() {
        return DEBUG;
    }
}
