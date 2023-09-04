package six.eared.macaque.agent.asm2;

import java.util.List;

public class AsmMethod {

    private int modifier;

    private String methodName;

    private String methodSign;

    private List<String> byteCode;

    private boolean newMethod;

    private boolean deleted;

    public boolean isNewMethod() {
        return newMethod;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public boolean isStatic() {
        return false;
    }
}
