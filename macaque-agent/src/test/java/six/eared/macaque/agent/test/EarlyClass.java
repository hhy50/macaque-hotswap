package six.eared.macaque.agent.test;


import java.lang.reflect.Field;

public class EarlyClass extends AbsEarlyClass2 {

    public byte a = 1;
    private short b = 2;
    public int c = 3;
    private long d = 4;
    private float e = 5;
    private double f = 6;
    protected char g = 7;
    protected boolean h = true;
    protected Object i = 9;
    public Integer j = 10;
    public String test1() {
        System.out.println("test1");
        return "test1";
    }

    public String test2() {
        return "test2";
    }
}
