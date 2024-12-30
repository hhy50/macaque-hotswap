package six.eared.macaque.preload;

import io.github.hhy50.linker.LinkerFactory;
import io.github.hhy50.linker.exceptions.LinkerException;

public class PatchedInvocation {
    private Object origin;
    private Object[] args;
    private String accessor;
    private PatchMethodInvoker invoker;

    public PatchedInvocation(Object origin, String patchClass, String accessor, Object[] args)
            throws ClassNotFoundException, LinkerException {
        this.origin = origin;
        this.invoker = LinkerFactory.createStaticLinker(PatchMethodInvoker.class, Class.forName(patchClass));
        this.args = args;
        this.accessor = accessor;
    }

    public Object[] getArgs() {
        return args;
    }

    public Object invoke() throws Exception {
        Object[] realArgs = new Object[args.length+1];
        System.arraycopy(this.args, 0, realArgs, 1, args.length);
        realArgs[0] = Class.forName(accessor).getConstructors()[0].newInstance(this.getOriginObject());
        return invoker.invoke(realArgs);
    }

    public Object getOriginObject() {
        return origin;
    }
}
