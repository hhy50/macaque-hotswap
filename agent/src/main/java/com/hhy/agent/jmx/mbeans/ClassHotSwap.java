package com.hhy.agent.jmx.mbeans;

import com.hhy.agent.env.Context;
import com.hhy.common.mbean.MBeanObjectName;
import com.hhy.common.rmi.ClassHotSwapRmiData;
import com.hhy.common.rmi.RmiResult;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;

public class ClassHotSwap implements ClassHotSwapMBean {

    @Override
    public RmiResult process(ClassHotSwapRmiData data) {
        String errMsg = null;
        try {
            Instrumentation inst = Context.INST;
            ClassDefinition classDefinition = new ClassDefinition(Class.forName(data.getClassName()), data.getNewClassByte());

            inst.redefineClasses(classDefinition);
            return RmiResult.success();
        } catch (Exception e) {
            errMsg = e.getMessage();

            if (errMsg == null || "".equals(errMsg)) {
                errMsg = e.getCause().getMessage();
            }
        }
        return RmiResult.error(errMsg);
    }

    @Override
    public ObjectName getMBeanName() throws MalformedObjectNameException {
        return new ObjectName(MBeanObjectName.HOT_SWAP_MBEAN);
    }
}
