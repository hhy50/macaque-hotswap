package com.hhy.server.jmx;

import com.hhy.common.mbean.MBean;

import javax.management.JMX;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;

public class JmxClient {

    private String ip;
    private Integer port;

    private JMXConnector connector;


    public JmxClient(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }

    public void connect() throws IOException {
        try {
            // service:jmx:rmi:///jndi/rmi://0.0.0.0:3300/macaque
            String url = String.format("service:jmx:rmi:///jndi/rmi://%s:%d/macaque", ip, port);
            JMXServiceURL serviceURL = new JMXServiceURL(url);

            this.connector = JMXConnectorFactory.connect(serviceURL);
        } catch (IOException e) {
            System.out.println("JMX link fail");
            e.printStackTrace();
            throw e;
        }
    }

    public MBean getMBean(String objectName) {
        try {
            return JMX.newMBeanProxy(this.connector.getMBeanServerConnection(),
                    new ObjectName(objectName), MBean.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void distory() {
        try {
            this.connector.close();
        } catch (Exception e) {

        }
    }
}
