package six.eared.macaque.agent.accessor;

public interface AccessorRule {


    /**
     * 将方法的访问规则转发到目标方法
     *
     * @param targetOwner
     * @param targetMethodName
     * @param targetDesc
     * @return
     */
    static AccessorRule forward(boolean isStatic, String targetOwner, String targetMethodName, String targetDesc) {
        return new MethodForwardAccess(isStatic, targetOwner, targetMethodName, targetDesc);
    }

    static AccessorRule direct() {
        return MethodDirectAccess.INSTANCE;
    }
}
