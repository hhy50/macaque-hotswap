package six.eared.macaque.agent.test;


public class EarlyClass extends AbsEarlyClass2 {

    public String test1() {
        System.out.println("test1");
        return "test1";
    }

    public String test2() {
        throw new NoSuchMethodError("aaa");
    }
}
