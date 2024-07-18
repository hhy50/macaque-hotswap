package six.eared.macaque.agent.test;

public class TestAddMethodClass2 {

    public String test1() {
        return "test1";
    }

    public String test2() {
        return staticMethod(
                ""+getInt(),
                String.valueOf(2),
                "3"+"4"
        );
    }

    public String staticMethod(String str1, String str2, String str3) {
        return str1+str2+str3;
    }

    public static int getInt() {
        return 1;
    }
}
