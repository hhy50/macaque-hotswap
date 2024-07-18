package six.eared.macaque.agent.test;

public class TestAddMethodClass {
    public String test1() {
        return test2();
    }
    public String test2() {
        return test3();
    }
    public static String test3() {
        return "static test3";
    }
}