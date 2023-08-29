package six.eared.macaque.agent.env;

import java.lang.instrument.Instrumentation;

/**
 * 环境变量
 */
public class Environment {

    /**
     * 初始化标识
     */
    private static boolean INIT_FLAG = false;

    /**
     * 是否开启debug
     */
    private static volatile boolean DEBUG = false;

    /**
     * Instrumentation实例
     * 用于重新对类进行加载
     */
    private static volatile Instrumentation INST = null;

    /**
     * 初始化环境变量
     *
     * @param debug 是否开启debug
     * @param inst  Instrumentation实例
     */
    public synchronized static void initEnv(boolean debug, Instrumentation inst) {
        if (INIT_FLAG) {
            return;
        }

        Environment.DEBUG = debug;
        Environment.INST = inst;

        INIT_FLAG = true;
    }

    public static boolean isDebug() {
        return DEBUG;
    }

    public static Instrumentation getInst() {
        return INST;
    }

}
