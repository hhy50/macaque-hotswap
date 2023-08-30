package six.eared.macaque.core.attach;

public interface AttachFactory {

    /**
     *
     * @param
     * @return
     */
    public Attach createRuntimeAttach(Integer pid);
}
