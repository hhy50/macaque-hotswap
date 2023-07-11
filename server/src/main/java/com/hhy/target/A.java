package com.hhy.target;

public class A {
    public void exec() {
        System.out.println("hello, word");
        System.out.println("target pid" + ProcessHandle.current().pid());

    }
}
