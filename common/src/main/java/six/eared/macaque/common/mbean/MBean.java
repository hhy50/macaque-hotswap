package six.eared.macaque.common.mbean;

import six.eared.macaque.common.rmi.RmiData;
import six.eared.macaque.common.rmi.RmiResult;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

public interface MBean<T extends RmiData> {

    /**
     *
     * @param data
     * @return
     */
    RmiResult process(T data);

    /**
     *
     * @return
     * @throws MalformedObjectNameException
     */
    ObjectName getMBeanName() throws MalformedObjectNameException;
}
