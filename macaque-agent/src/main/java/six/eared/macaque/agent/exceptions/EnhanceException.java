package six.eared.macaque.agent.exceptions;

public class EnhanceException extends RuntimeException {

    public EnhanceException(Exception e) {
        super(e);
    }

    public EnhanceException(String s) {
        super(s);
    }
}
