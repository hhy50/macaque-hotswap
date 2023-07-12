package com.hhy.server.test.target;

public class Main {

    /**
     * -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=3300 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false
     * @param args
     */
    public static void main(String[] args) {
        A a = new A();
        while (true) {
            try {
                a.exec();
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
