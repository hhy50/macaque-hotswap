package six.eared.macaque.mbean;



import six.eared.macaque.mbean.rmi.RmiData;
import six.eared.macaque.mbean.rmi.RmiResult;

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
