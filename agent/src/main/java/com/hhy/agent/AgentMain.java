package com.hhy.agent;


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
            bootstrapClass = classLoader.loadClass("com.hhy.agent.ApplicationBootstrap");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (bootstrapClass != null) {
            try {
                Method init = bootstrapClass.getMethod("start", String.class, Instrumentation.class);
                init.invoke(null, args, inst);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("attach success");
        }
    }

    private static ClassLoader getClassLoader() {
        return ClassLoader.getSystemClassLoader();
    }
}
