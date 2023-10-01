package six.eared.macaque.agent.test;

public class EarlyClass extends AbsEarlyClass {
    public String test1() {
        System.out.println("test1");
        return "test1";
    }

    public String test2() {
        System.out.println("test2");
        return "test2";
    }

    public final String test3() {
        System.out.println("test3");
        return "test3";
    }

    public class Macaque_Accessor {
        public String test1() {
            return EarlyClass.this.test1();
        }

        public String test2() {
            return EarlyClass.this.test2();
        }

        public String test3() {
            return EarlyClass.this.test3();
        }
    }
}
