package six.eared.macaque.agent;


import six.eared.macaque.agent.env.Environment;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;

/**
 * 探针主类
 */
public class AgentMain {

    public static void agentmain(String args, Instrumentation inst) {
        loadBootstrap(args, inst);
    }

    /**
     * 加载引导类
     *
     * @param args 参数
     * @param inst inst
     */
    private static void loadBootstrap(String args, Instrumentation inst) {
        ClassLoader classLoader = getClassLoader();
        Class<?> bootstrapClass = null;
        try {
            // 加载引导类
            bootstrapClass = classLoader.loadClass("six.eared.macaque.agent.AgentBootstrap");
        } catch (Exception e) {
            if (Environment.isDebug()) {
                System.out.println("load AgentBootstrap.class error");
                e.printStackTrace();
            }
        }

        if (bootstrapClass != null) {
            try {
                // 调用引导类的start方法
                Method init = bootstrapClass.getMethod("start", String.class, Instrumentation.class);
                init.invoke(null, args, inst);
            } catch (Exception e) {
                if (Environment.isDebug()) {
                    System.out.println("AgentBootstrap.start error");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取系统类加载器
     *
     * @return 类加载器
     */
    private static ClassLoader getClassLoader() {
        return ClassLoader.getSystemClassLoader();
    }
}
