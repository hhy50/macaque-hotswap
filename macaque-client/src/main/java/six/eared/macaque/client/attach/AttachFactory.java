package six.eared.macaque.client.attach;

public interface AttachFactory {

    /**
     *
     * @param
     * @return
     */
    public Attach createRuntimeAttach(Integer pid);
}
