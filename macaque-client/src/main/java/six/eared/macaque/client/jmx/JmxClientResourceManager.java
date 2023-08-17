package six.eared.macaque.client.jmx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import six.eared.macaque.client.common.PortNumberGenerator;

import java.util.HashMap;
import java.util.Map;

public class JmxClientResourceManager {

    private static final Logger log = LoggerFactory.getLogger(JmxClient.class);

    private static final Map<String, JmxClient> connectPool = new HashMap();

    private static final JmxClientResourceManager INSTANCE = new JmxClientResourceManager();

    private JmxClientResourceManager() {

    }

    public static JmxClientResourceManager getInstance() {
        return INSTANCE;
    }

    public synchronized JmxClient getResource(String pid) {
        JmxClient jmxClient = connectPool.get(pid);
        try {
            if (jmxClient == null || !jmxClient.isConnect()) {
                jmxClient = createResource(pid);
            }
        } catch (Exception e) {
            log.error("jmx getResource error", e);
        }
        return jmxClient;
    }

    public synchronized JmxClient createResource(String pid) {
        JmxClient jmxClient = new JmxClient("127.0.0.1",
                PortNumberGenerator.getPort(Integer.parseInt(pid)));

        if (jmxClient.connect()) {
            connectPool.put(pid, jmxClient);
            return jmxClient;
        }
        return null;
    }
}
