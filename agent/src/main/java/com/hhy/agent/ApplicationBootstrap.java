package com.hhy.agent;

import com.hhy.agent.env.Context;
import com.hhy.agent.jmx.JmxMBeanManager;

import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import java.io.ByteArrayInputStream;
import java.lang.instrument.Instrumentation;
import java.rmi.registry.LocateRegistry;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class ApplicationBootstrap {

    private static final AtomicBoolean START_FLAG = new AtomicBoolean(false);


    public static JmxMBeanManager JMX_MBEAN_MANAGER;

    public static void start(String args, Instrumentation inst) {
        if (!START_FLAG.get()) {
            Properties properties = parseArgs(args);
            try {
                Context.INST = inst;
                int jmxPort = Integer.parseInt(properties.getProperty("port", "3030"));

                // init jmx, mbeans
                JMX_MBEAN_MANAGER = initJmxService(jmxPort);

                START_FLAG.set(true);
                System.out.printf("attach success, jmx port=%d\n",
                        jmxPort);
            } catch (Exception e) {

            }
        }
    }

    private static JmxMBeanManager initJmxService(int port) {
        JmxMBeanManager jmxMBeanManager = new JmxMBeanManager();

        try {
            LocateRegistry.createRegistry(port);
            JMXServiceURL url = new JMXServiceURL(String.format("service:jmx:rmi:///jndi/rmi://0.0.0.0:%d/macaque", port));
            JMXConnectorServer jcs = JMXConnectorServerFactory.newJMXConnectorServer(url,
                    null, jmxMBeanManager.getMBeanServer());
            jcs.start();

            return jmxMBeanManager;
        } catch (Exception e) {

        }
        return null;
    }

    private static Properties parseArgs(String args) {
        try {
            if (args != null && args.length() > 0) {
                String s = args.replaceAll(" ", "")
                        .replaceAll(",", "\n");
                Properties properties = new Properties();
                properties.load(new ByteArrayInputStream(s.getBytes()));
                return properties;
            }
        } catch (Exception e) {

        }
        return new Properties();
    }
}
