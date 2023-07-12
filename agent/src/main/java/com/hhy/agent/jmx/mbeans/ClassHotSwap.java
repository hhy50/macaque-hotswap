package com.hhy.agent.jmx.mbeans;

import com.hhy.common.mbean.MBeanObjectName;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.Arrays;

public class ClassHotSwap implements ClassHotSwapMBean {
    @Override
    public void process(String[] args) {
        System.out.println(Arrays.toString(args));
    }

    @Override
    public ObjectName getMBeanName() {
        try {
            return new ObjectName(MBeanObjectName.HOT_SWAP_MBEAN);
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }
}
