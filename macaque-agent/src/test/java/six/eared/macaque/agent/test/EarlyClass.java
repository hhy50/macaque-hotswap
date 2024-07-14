package six.eared.macaque.agent.test;


public class EarlyClass extends AbsEarlyClass2 {


    public Utils utils = new Utils();

    public String test1() {
        System.out.println("test1");
        return "test1";
    }

    public String test2() {
        return "test2";
    }

    class Accessor {

        public Utils macaque$field$utils() {
            return utils;
        }
    }
}
