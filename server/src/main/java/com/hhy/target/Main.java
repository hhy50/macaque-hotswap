package com.hhy.target;

public class Main {
    public static void main(String[] args) {
        A a = new A();
        while (true) {
            try {
                a.exec();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
