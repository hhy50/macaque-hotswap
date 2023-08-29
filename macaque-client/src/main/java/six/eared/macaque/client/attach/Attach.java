package six.eared.macaque.client.attach;

public interface Attach {

    /**
     * attach process
     */
    public int attach(String agentpath, String property);
}
