package six.eared.macaque.agent.jmx.mbeans;


import six.eared.macaque.mbean.MBeanObjectName;
import six.eared.macaque.mbean.rmi.EmptyRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;


/**
 * 心跳MBean
 * 用于检测Agent是否存活
 */
public class JmxHeartbeat implements JmxHeartbeatMBean {

    /**
     * 心跳
     *
     * @param data 心跳数据
     * @return 心跳结果
     */
    @Override
    public RmiResult process(EmptyRmiData data) {
        return RmiResult.success();
    }

    /**
     * 获取MBean名称
     *
     * @return MBean名称
     * @throws MalformedObjectNameException MBean名称异常
     */
    @Override
    public ObjectName getMBeanName() throws MalformedObjectNameException {
        return new ObjectName(MBeanObjectName.HEART_BEAT_MBEAN);
    }
}
