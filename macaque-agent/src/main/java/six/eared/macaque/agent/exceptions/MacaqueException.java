package six.eared.macaque.agent.exceptions;


public abstract class MacaqueException extends RuntimeException {

    protected String details;

    public MacaqueException(String details) {
        super(details);
        this.details = details;
    }

    public MacaqueException(Throwable e) {
        super(e);
    }

    public String getDetails() {
        return this.details;
    }
}
