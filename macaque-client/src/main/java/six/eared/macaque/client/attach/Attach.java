package six.eared.macaque.client.attach;

public interface Attach {

    /**
     * attach process
     */
    public boolean attach(String agentpath, String property);
}
