package six.eared.macaque.agent.test;


public class EarlyClass extends AbsEarlyClass2 {

    public String test1() {
        System.out.println("test1");
        return "test1";
    }

    public String test2() {
        System.out.println("test2");
        return "after test2";
    }

    public String _newMethod() {
        System.out.println("test3");
        return "_newMethod";
    }
}
