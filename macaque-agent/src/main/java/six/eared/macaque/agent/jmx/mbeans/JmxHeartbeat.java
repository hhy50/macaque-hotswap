package six.eared.macaque.agent.jmx.mbeans;

import six.eared.macaque.common.rmi.EmptyRmiData;
import six.eared.macaque.common.rmi.RmiResult;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import static six.eared.macaque.common.mbean.MBeanObjectName.HEART_BEAT_MBEAN;

public class JmxHeartbeat implements JmxHeartbeatMBean {

    @Override
    public RmiResult process(EmptyRmiData data) {
        return RmiResult.success();
    }

    @Override
    public ObjectName getMBeanName() throws MalformedObjectNameException {
        return new ObjectName(HEART_BEAT_MBEAN);
    }
}
