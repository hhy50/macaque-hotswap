package six.eared.macaque.agent.test;

public class EarlyClass2 extends AbsEarlyClass2 {

    public String test1(String aaa) {
        System.out.println("test1");
        return test2();
    }

    public String test2() {
        System.out.println("test2");
        return testAAA();
    }

    public static String testAAA() {
        return "testAAA";
    }
}