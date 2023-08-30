package six.eared.macaque.core.jmx;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JmxResourceManager {

    private static final Map<Integer, JmxClient> CLIENTS = new ConcurrentHashMap<>();

    public JmxClient getResource(Integer pid) {
        return CLIENTS.get(pid);
    }

    public void addResource(Integer pid, JmxClient client) {
        CLIENTS.put(pid, client);
    }
}
