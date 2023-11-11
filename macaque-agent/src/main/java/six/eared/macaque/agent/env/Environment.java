package six.eared.macaque.agent.env;

import six.eared.macaque.common.util.FileUtil;

import java.io.File;
import java.io.IOException;
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

    /**
     *
     * @return
     */
    public static boolean isDebug() {
        return DEBUG;
    }

    /**
     *
     * @return
     */
    public static Instrumentation getInst() {
        return INST;
    }

    /**
     *
     * @return
     */
    public static String getAndInitTmpClasspath() throws IOException {
        File tmpFile = new File(String.format("%s/classpath/", FileUtil.getProcessTmpPath()));
        if (!tmpFile.exists()) {
            tmpFile.mkdirs();
            return tmpFile.getPath();
        }
        FileUtil.deleteFile(tmpFile);
        return tmpFile.getPath();
    }
}
