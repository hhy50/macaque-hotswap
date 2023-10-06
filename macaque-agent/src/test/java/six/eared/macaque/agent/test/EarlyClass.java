package six.eared.macaque.agent.test;


public class EarlyClass extends AbsEarlyClass2 {

    public String test1() {
        System.out.println("test1");
        return "test1";
    }

    public String test2() {
        System.out.println("test2");
        return "test2";
    }

    public String test3() {
        System.out.println("test3");
        return "test3";
    }

    private String test5(String aa, int bb, int cc, Object obj) {
        return EarlyClass$Macaque_test5.test5(this.new Macaque_Accessor(), aa, bb, cc, obj);
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
