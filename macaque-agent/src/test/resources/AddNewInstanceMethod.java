package six.eared.macaque.agent.test.compatibility;
public class EarlyClass {

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
        return test4();
    }
    public static String test4() {
        return "test4";
    }
}