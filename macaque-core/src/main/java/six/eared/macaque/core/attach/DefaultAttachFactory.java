package six.eared.macaque.core.attach;


public class DefaultAttachFactory implements AttachFactory {

    private static final DefaultAttachFactory INSTANCE = new DefaultAttachFactory();

    @Override
    public synchronized Attach createRuntimeAttach(Integer pid) {
        RuntimeAttach runtimeAttach = new RuntimeAttach(pid);
        return runtimeAttach;
    }

    public static DefaultAttachFactory getInstance() {
        return INSTANCE;
    }
}
