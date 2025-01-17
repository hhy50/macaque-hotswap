package six.eared.macaque.preload;

public class PatchedInvocation {
    private Object origin;
    private Object[] args;
    private Class<?> accessor;
    private PatchMethodInvoker invoker;

    public PatchedInvocation(Object origin, PatchMethodInvoker invoker, Class<?> accessor, Object[] args) {
        this.origin = origin;
        this.invoker = invoker;
        this.args = args;
        this.accessor = accessor;
    }

    public Object[] getArgs() {
        return args;
    }

    public Object invoke() throws Exception {
        Object[] realArgs = new Object[args.length+1];
        System.arraycopy(this.args, 0, realArgs, 1, args.length);
        realArgs[0] = accessor.getConstructors()[0].newInstance(this.getOriginObject());
        return invoker.invoke(realArgs);
    }

    public Object getOriginObject() {
        return origin;
    }
}
