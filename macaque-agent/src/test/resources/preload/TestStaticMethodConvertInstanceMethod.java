package six.eared.macaque.agent.test;

/**
 * 1. 静态方法转实例方法
 */
public class TestStaticMethodConvertInstanceMethod {

    public String test1() {
        return "test1";
    }

    public String test2() {
        return "test2";
    }

    public static String staticMethod(String str1, String str2, String str3) {
        return str1+str2+str3;
    }
}