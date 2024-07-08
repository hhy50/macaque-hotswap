package six.eared.macaque.agent.test;

public class EarlyClass2 extends AbsEarlyClass2 {

    public String test1(String name) {
        return _newMethod(name, "1111");
    }

    public String test2() {
        return "test2";
    }

    public String _newMethod(String arg1, String arg2) {
        return "arg1=" + arg1 + ",arg2=" + arg2;
    }
}
