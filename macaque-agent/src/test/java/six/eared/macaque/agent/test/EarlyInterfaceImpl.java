package six.eared.macaque.agent.test;

public class EarlyInterfaceImpl implements EarlyInterface {

    @Override
    public String test1() {
        return EarlyInterface.super.test1();
    }
}
