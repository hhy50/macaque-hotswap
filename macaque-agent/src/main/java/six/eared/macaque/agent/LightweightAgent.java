package six.eared.macaque.agent;

import six.eared.macaque.agent.env.Environment;

import java.lang.instrument.Instrumentation;

public class LightweightAgent {

    public static void agentmain(String args, Instrumentation inst) {
        Environment.initEnv(true, inst);
        System.out.println("attach success, initEnv()");
    }
}
