package six.eared.macaque.agent.jmx.mbeans;

import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.mbean.MBeanObjectName;
import six.eared.macaque.mbean.rmi.ClassHotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;

public class ClassHotSwap implements ClassHotSwapMBean {

    @Override
    public RmiResult process(ClassHotSwapRmiData request) {
        String errMsg = null;
        try {
            Instrumentation inst = Environment.getInst();
            List<ClassDefinition> needRedefineClass = new ArrayList<>();
            for (Class<?> clazz : inst.getAllLoadedClasses()) {
                if (clazz.getName().equals(request.getClassName())) {
                    needRedefineClass.add(new ClassDefinition(clazz, request.getNewClassByte()));
                }
            }
            if (Environment.isDebug()) {
                System.out.printf("[ClassHotSwap.process] className:[%s], find class instance count: [%d]%n", request.getClassName(), needRedefineClass.size());
            }
            inst.redefineClasses(needRedefineClass.toArray(new ClassDefinition[0]));
            return RmiResult.success();
        } catch (Exception e) {
            if (Environment.isDebug()) {
                System.out.println("ClassHotSwap.process error");
                e.printStackTrace();
            }
        }
        return RmiResult.error(errMsg);
    }

    @Override
    public ObjectName getMBeanName() throws MalformedObjectNameException {
        return new ObjectName(MBeanObjectName.HOT_SWAP_MBEAN);
    }
}
