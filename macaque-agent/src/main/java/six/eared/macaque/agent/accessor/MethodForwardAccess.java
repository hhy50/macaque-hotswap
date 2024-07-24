package six.eared.macaque.agent.accessor;


import lombok.Data;

@Data
public class MethodForwardAccess implements AccessorRule {
    private final boolean isStatic;
    private final String targetOwner;
    private final String targetMethodName;
    private final String targetDesc;

    public MethodForwardAccess(boolean isStatic, String targetOwner, String targetMethodName, String targetDesc) {
        this.isStatic = isStatic;
        this.targetOwner = targetOwner;
        this.targetMethodName = targetMethodName;
        this.targetDesc = targetDesc;
    }
}
