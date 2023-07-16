package six.eared.macaque.agent;


import six.eared.macaque.agent.env.Environment;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;

public class AgentMain {

    public static void agentmain(String args, Instrumentation inst) {
        loadBootstrap(args, inst);
    }

    private static void loadBootstrap(String args, Instrumentation inst) {
        ClassLoader classLoader = getClassLoader();
        Class<?> bootstrapClass = null;
        try {
            bootstrapClass = classLoader.loadClass("six.eared.macaque.agent.AgentBootstrap");
        } catch (Exception e) {
            if (Environment.isDebug()) {
                System.out.println("load AgentBootstrap.class error");
                e.printStackTrace();
            }
        }

        if (bootstrapClass != null) {
            try {
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

    private static ClassLoader getClassLoader() {
        return ClassLoader.getSystemClassLoader();
    }
}
