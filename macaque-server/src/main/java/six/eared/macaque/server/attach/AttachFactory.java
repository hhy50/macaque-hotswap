package six.eared.macaque.server.attach;

public interface AttachFactory {

    /**
     *
     * @param
     * @return
     */
    public Attach createRuntimeAttach(String pid);
}
