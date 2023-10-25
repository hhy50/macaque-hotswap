package six.eared.macaque.agent.test;


public class EarlyClass extends AbsEarlyClass2 {

    public String test1() {
        System.out.println("test1");
        return "test1";
    }

    public String test2() {
        System.out.println("test2");
        return "test2";
    }

    public String test3() {
        System.out.println("test3");
        return "test3";
    }

    private String aaaaa() {
        System.out.println("test4");
        System.out.println("test4");
        return "test4";
    }

    public String test5() {
        System.out.println("test4");
        System.out.println("test4");
        return "test4";
    }
}
