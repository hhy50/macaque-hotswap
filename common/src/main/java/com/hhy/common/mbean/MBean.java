package com.hhy.common.mbean;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

public interface MBean {

    void process(String[] args);

    ObjectName getMBeanName() throws MalformedObjectNameException;
}
