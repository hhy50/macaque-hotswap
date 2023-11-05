package six.eared.macaque.agent.exceptions;

public class HotswapException extends MacaqueException {

    public HotswapException(Throwable e) {
        super(e);
    }

    public HotswapException(String details) {
        super(details);
    }
}
