package six.eared.macaque.agent.env;

import java.lang.instrument.Instrumentation;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 环境变量
 */
public class Environment {

    /**
     * 初始化标识
     */
    private static final AtomicBoolean INIT_FLAG = new AtomicBoolean(false);
    /**
     * 是否开启debug
     */
    private static boolean DEBUG = false;
    /**
     * Instrumentation实例
     * 用于重新对类进行加载
     */
    private static Instrumentation INST = null;

    /**
     * 初始化环境变量
     *
     * @param debug 是否开启debug
     * @param inst  Instrumentation实例
     */
    public synchronized static void initEnv(boolean debug, Instrumentation inst) {
        if (INIT_FLAG.compareAndSet(false, true)) {
            Environment.DEBUG = debug;
            Environment.INST = inst;
        }
    }

    public static boolean isDebug() {
        return DEBUG;
    }

    public static Instrumentation getInst() {
        return INST;
    }
}
