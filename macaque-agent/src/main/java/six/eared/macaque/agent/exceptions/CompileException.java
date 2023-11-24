package six.eared.macaque.agent.exceptions;

public class CompileException extends MacaqueException {

    public CompileException(String details) {
        super(details);
    }

    public CompileException(Exception e) {
        super(e);
    }
}
