package six.eared.macaque.agent.test;

public class TestAddMethodClass {
    public Object field1 = "1234";
    public static Object field2 = "1234";
    public String test1() {
        return "test1";
    }

    public String test2() {
        return test4();
    }

    public static String test3() {
        return "test3";
    }

    /**
     * new method
     *
     * @return
     */
    public String test4() {
        field1 = "1234";
        field2 = "5678";
        return (String) field1+field2+test1()+test3();
    }
}