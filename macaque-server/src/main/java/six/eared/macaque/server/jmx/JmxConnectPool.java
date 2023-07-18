package six.eared.macaque.server.jmx;


import java.util.HashMap;

public class JmxConnectPool extends HashMap<String, JmxClient> {

    public void add(String pid, JmxClient jmxClient) {
        super.put(pid, jmxClient);
    }
}
