package six.eared.macaque.server.jmx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import six.eared.macaque.mbean.MBean;
import six.eared.macaque.mbean.MBeanObjectName;
import six.eared.macaque.mbean.rmi.EmptyRmiData;
import six.eared.macaque.mbean.rmi.RmiData;

import javax.management.JMX;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;

public class JmxClient {

    private static final Logger log = LoggerFactory.getLogger(JmxClient.class);

    private String ip;

    private Integer port;

    private JMXConnector connector;

    private MBean<EmptyRmiData> hearbeatMBean;

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
            this.hearbeatMBean = getMBean(MBeanObjectName.HEART_BEAT_MBEAN);
            log.info("connect success url: {}", url);
            return true;
        } catch (IOException e) {
            log.error("connect error, url:" + url, e);
        }
        return false;
    }

    public boolean isConnect() {
        try {
            return connector != null && this.hearbeatMBean != null && this.hearbeatMBean.process(new EmptyRmiData()).isSuccess();
        } catch (Exception e) {
            log.error("isConnect error", e);
            return false;
        }
    }

    public <T extends RmiData> MBean<T> getMBean(String objectName) {
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
