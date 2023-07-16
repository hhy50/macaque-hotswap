package six.eared.macaque.agent.jmx.mbeans;

import six.eared.macaque.agent.env.Context;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.common.mbean.MBeanObjectName;
import six.eared.macaque.common.rmi.ClassHotSwapRmiData;
import six.eared.macaque.common.rmi.RmiResult;

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
            if (Environment.isDebug()) {
                System.out.println("ClassHotSwap.process error");
                e.printStackTrace();
            }

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
