package six.eared.macaque.agent.test;

public class TestAddMethodClass {

    public String test1() {
        return _newMethod("1111", "2222");
    }

    public String test2() {
        return "test2";
    }

    public static String _newMethod(String arg1, String arg2) {
        return "arg1=" + arg1 + ",arg2=" + arg2;
    }
}
