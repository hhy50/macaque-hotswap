package com.hhy.common.mbean;

import com.hhy.common.rmi.RmiData;
import com.hhy.common.rmi.RmiResult;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

public interface MBean<T extends RmiData> {

    RmiResult process(T data);

    ObjectName getMBeanName() throws MalformedObjectNameException;
}
