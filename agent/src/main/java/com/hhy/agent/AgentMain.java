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

        }

        if (bootstrapClass != null) {
            try {
                Method init = bootstrapClass.getMethod("start", String.class, Instrumentation.class);
                init.invoke(null, args, inst);
            } catch (Exception e) {

            }
        }
    }

    private static ClassLoader getClassLoader() {
        return ClassLoader.getSystemClassLoader();
    }
}
