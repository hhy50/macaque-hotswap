package com.hhy.server.test.target;

public class A {
    public void exec() {
        // -1
        long pid = ProcessHandle.current().pid();
        ProcessHandle.current().pid();
        System.out.println("hello");
        System.out.println("target pid: " + ProcessHandle.current().pid());
    }
}
