package six.eared.macaque.test.test1;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        A a = new A();
        while (true) {
            a.exec();
            Thread.sleep(5000);
        }
    }
}
