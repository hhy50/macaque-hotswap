package six.eared.macaque.server.attach;

public interface Attach {

    /**
     * attach process
     * @param pid
     */
    public boolean attach(String pid);
}
