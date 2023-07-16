package six.eared.macaque.server.jmx;

import six.eared.macaque.common.mbean.MBean;
import six.eared.macaque.server.config.LoggerName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.JMX;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;

public class JmxClient {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.auto());

    private String ip;
    private Integer port;

    private JMXConnector connector;

    public JmxClient(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }

    public boolean connect() throws IOException {
        String url = null;
        try {
            url = String.format("service:jmx:rmi:///jndi/rmi://%s:%d/macaque", ip, port);
            JMXServiceURL serviceURL = new JMXServiceURL(url);
            this.connector = JMXConnectorFactory.connect(serviceURL);
            log.info("connect success url: {}", url);
            return true;
        } catch (IOException e) {
            log.error("connect error, url:" + url, e);
        }
        return false;
    }

    public boolean isConnect() {
        return connector != null;
    }

    public MBean getMBean(String objectName) {
        try {
            return JMX.newMBeanProxy(this.connector.getMBeanServerConnection(),
                    new ObjectName(objectName), MBean.class);
        } catch (Exception e) {
            log.error("getMBean error", e);
        }
        return null;
    }

    public void distory() {
        try {
            this.connector.close();
        } catch (Exception e) {
            log.error("distory error", e);
        }
    }
}
