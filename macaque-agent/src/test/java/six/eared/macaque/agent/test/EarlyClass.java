package six.eared.macaque.agent.test;

import java.lang.invoke.MethodHandles;

public class EarlyClass extends AbsEarlyClass2 {
    private MethodHandles.Lookup lookup;

    public String test1() {
        System.out.println("test1");
        return "test1";
    }

    public String test2() {
        System.out.println("test2");
        return "test2";
    }

    public String test3() {
        return "test3";
    }

    public String test5() {
        return "test5";
    }

    private String aaaa(long arg1, long arg2, long arg3, long arg4, long arg5, long arg6, long arg7, long arg8, long arg9, long arg10,
                       int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10) {
        System.out.println("abs test4");
        return "abs2 test4";
    }

    private String bbbb(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9, Object arg10,
                       int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10) {
        System.out.println("abs test4");
        return "abs2 test4";
    }

    private String ccc(String a, String b) {
        return a + b +"cc";
    }

    public long test7(long arg1, long arg2, long arg3, long arg4, long arg5, long arg6, long arg7, long arg8, long arg9, long arg10,
                      int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10) throws Throwable {
        return arg1 + arg2;
    }
}
