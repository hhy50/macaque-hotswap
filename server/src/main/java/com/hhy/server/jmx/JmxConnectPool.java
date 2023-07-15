package com.hhy.server.jmx;

import com.hhy.server.common.PortNumberGenerator;
import com.hhy.server.config.LoggerName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JmxConnectPool {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.auto());

    private static final JmxConnectPool INSTANCE = new JmxConnectPool();

    private JmxConnectPool() {

    }

    public static JmxConnectPool getINSTANCE() {
        return INSTANCE;
    }

    private Map<String, JmxClient> pool = new ConcurrentHashMap<>();

    public synchronized JmxClient getResource(String pid) {
        JmxClient jmxClient = pool.get(pid);
        if (jmxClient == null) {
            jmxClient = new JmxClient("127.0.0.1",
                    PortNumberGenerator.getPort(Integer.parseInt(pid)));
            try {
                if (jmxClient.connect()) {
                    pool.put(pid, jmxClient);
                } else {
                    jmxClient = null;
                }
            } catch (IOException e) {
                log.error("getResource error", e);
            }
        }
        return jmxClient;
    }
}
