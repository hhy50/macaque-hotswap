package six.eared.macaque.agent.test;

public class TestStaticMethodConvertInstanceMethod {

    public String test1() {
        return "test1";
    }

    public String test2() {
        return staticMethod("1", "2", "3");
    }

    public String staticMethod(String str1, String str2, String str3) {
        return str1+str2+str3;
    }
}
