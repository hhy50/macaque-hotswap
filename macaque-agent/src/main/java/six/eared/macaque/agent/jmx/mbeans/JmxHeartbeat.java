package six.eared.macaque.agent.jmx.mbeans;


import six.eared.macaque.mbean.MBeanObjectName;
import six.eared.macaque.mbean.rmi.EmptyRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;


public class JmxHeartbeat implements JmxHeartbeatMBean {

    @Override
    public RmiResult process(EmptyRmiData data) {
        return RmiResult.success();
    }

    @Override
    public ObjectName getMBeanName() throws MalformedObjectNameException {
        return new ObjectName(MBeanObjectName.HEART_BEAT_MBEAN);
    }
}
