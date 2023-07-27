package six.eared.macaque.server.jmx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import six.eared.macaque.server.common.PortNumberGenerator;

import java.io.IOException;

public class JmxClientResourceManager {

    private static final Logger log = LoggerFactory.getLogger(JmxClientResourceManager.class);

    private static final JmxConnectPool pool = new JmxConnectPool();

    private static final JmxClientResourceManager INSTANCE = new JmxClientResourceManager();

    private JmxClientResourceManager() {

    }

    public static JmxClientResourceManager getInstance() {
        return INSTANCE;
    }

    public synchronized JmxClient getResource(String pid) {
        JmxClient jmxClient = pool.get(pid);
        if (jmxClient == null) {
            jmxClient = createResource(pid);
        } else {
            if (!jmxClient.isConnect()) {
                jmxClient = createResource(pid);
            }
        }
        return jmxClient;
    }

    public synchronized JmxClient createResource(String pid) {
        JmxClient jmxClient = new JmxClient("127.0.0.1",
                PortNumberGenerator.getPort(Integer.parseInt(pid)));
        try {
            if (jmxClient.connect()) {
                pool.add(pid, jmxClient);
                return jmxClient;
            }
        } catch (IOException e) {
            log.error("createResource error", e);
        }
        return null;
    }
}
