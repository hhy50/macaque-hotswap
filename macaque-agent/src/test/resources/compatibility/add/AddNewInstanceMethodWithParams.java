package six.eared.macaque.agent.test;

public class TestAddMethodClass {

    public String test1() {
        return "test1";
    }

    public String test2() {
        return _newMethod("1", "2");
    }

    /**
     * new method
     *
     * @param arg1
     * @param arg2
     * @return
     */
    public String _newMethod(String arg1, String arg2) {
        return "arg1=" + arg1 + ",arg2=" + arg2;
    }
}
