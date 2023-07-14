package com.hhy.server.test.target;

public class A {
    public void exec() {
        System.out.println("target pid: " + ProcessHandle.current().pid());
    }
}
