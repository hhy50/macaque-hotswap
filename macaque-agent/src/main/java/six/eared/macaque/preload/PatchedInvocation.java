package six.eared.macaque.preload;

import lombok.Getter;

public class PatchedInvocation {
    /**
     *  原生对象
     */
    @Getter
    private Object origin;

    /**
     * 方法参数
     */
    @Getter
    private Object[] args;

    /**
     * 访问器
     */
    private Class<?> accessor;
    private PatchMethodInvoker invoker;

    public PatchedInvocation(Object origin, PatchMethodInvoker invoker, Class<?> accessor, Object[] args) {
        this.origin = origin;
        this.invoker = invoker;
        this.args = args;
        this.accessor = accessor;
    }

    public Object invoke() throws Exception {
        Object[] realArgs = new Object[args.length+1];
        System.arraycopy(this.args, 0, realArgs, 1, args.length);
        realArgs[0] = accessor.getConstructors()[0].newInstance(this.getOrigin());
        return invoker.invoke(realArgs);
    }
}
