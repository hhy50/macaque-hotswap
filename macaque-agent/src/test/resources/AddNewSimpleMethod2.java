package six.eared.macaque.agent.test;


public class EarlyClass extends AbsEarlyClass2 {

    public String test1() {
        System.out.println("test1");
        return "test1";
    }

    public String test2() {
        System.out.println("test2");
        return _newMethod(123, "456", "6789");
    }

    public String _newMethod(int a, String b, String c) {
        System.out.println(b+c+a);;
        return "_newMethod";
    }
}
