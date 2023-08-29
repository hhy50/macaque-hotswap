package six.eared.macaque.client.exception;

public class JmxConnectException extends RuntimeException{

    private String target;

    public JmxConnectException(String target, Exception e) {
        super(String.format("connect '%s' fail", target), e);
        this.target = target;
    }

    public String getTarget() {
        return target;
    }
}
