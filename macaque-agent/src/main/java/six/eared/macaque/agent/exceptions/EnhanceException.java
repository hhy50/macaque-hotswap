package six.eared.macaque.agent.exceptions;

public class EnhanceException extends MacaqueException {

    public EnhanceException(Exception e) {
        super(e);
    }

    public EnhanceException(String details) {
        super(details);
    }
}
