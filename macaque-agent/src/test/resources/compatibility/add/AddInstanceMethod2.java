package six.eared.macaque.agent.test;

import six.eared.macaque.agent.test.case1.TestAddMethodClass;

public class TestAddMethodClass2 extends TestAddMethodClass {

    /**
     * new method
     *
     * @return
     */
    public String test4() {
        field1 = "1234";
        field2 = "5678";
        field3 = "90";
        return (String) field1+field2+test1()+test3()+field3;
    }
}