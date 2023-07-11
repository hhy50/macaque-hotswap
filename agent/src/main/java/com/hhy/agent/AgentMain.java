package com.hhy.agent;

import java.lang.instrument.Instrumentation;

public class AgentMain {

    public static void agentmain(String args, Instrumentation inst) {
        System.out.println("agent pid" + ProcessHandle.current().pid());
    }
}
