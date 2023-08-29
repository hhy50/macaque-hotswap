package six.eared.macaque.client.c;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import six.eared.macaque.client.exception.JmxConnectException;
import six.eared.macaque.mbean.MBean;
import six.eared.macaque.mbean.MBeanObjectName;
import six.eared.macaque.mbean.rmi.EmptyRmiData;
import six.eared.macaque.mbean.rmi.RmiData;

import javax.management.JMX;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class JmxClient {

    private static final Logger log = LoggerFactory.getLogger(JmxClient.class);

    private String host;

    private Integer port;

    private JMXConnector connector;

    private MBean<EmptyRmiData> hearbeatMBean;

    public JmxClient(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    public void connect() {
        try {
            String url = String.format("service:jmx:rmi:///jndi/rmi://%s:%d/macaque", host, port);
            JMXServiceURL serviceURL = new JMXServiceURL(url);
            this.connector = JMXConnectorFactory.connect(serviceURL);
            this.hearbeatMBean = getMBean(MBeanObjectName.HEART_BEAT_MBEAN);
        } catch (Exception e) {
            throw new JmxConnectException(host + ":" + port, e);
        }
    }

    public void disconnect() {
        try {
            this.connector.close();
            this.connector = null;
            this.hearbeatMBean = null;
        } catch (Exception e) {
            log.warn("jmx disconnect error, {}", e.getMessage());
        }
    }

    public boolean isConnect() {
        try {
            return connector != null
                    && this.hearbeatMBean != null
                    && this.hearbeatMBean.process(new EmptyRmiData()).isSuccess();
        } catch (Exception e) {
            return false;
        }
    }

    public <T extends RmiData> MBean<T> getMBean(String objectName) throws Exception {
        return JMX.newMBeanProxy(this.connector.getMBeanServerConnection(),
                new ObjectName(objectName), MBean.class);
    }
}
