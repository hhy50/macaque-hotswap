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

/**
 * 热加载MBean
 */
public class ClassHotSwap implements ClassHotSwapMBean {

    /**
     * 热加载
     *
     * @param request 热加载数据
     * @return 热加载结果
     */
    @Override
    public RmiResult process(ClassHotSwapRmiData request) {
        String errMsg = null;
        try {
            Instrumentation inst = Environment.getInst();
            List<ClassDefinition> needRedefineClass = new ArrayList<>();
            // 遍历所有已加载的类
            for (Class<?> clazz : inst.getAllLoadedClasses()) {
                // 如果类名相同，则加入到需要重新定义的类列表中，用于热加载
                if (clazz.getName().equals(request.getClassName())) {
                    needRedefineClass.add(new ClassDefinition(clazz, request.getNewClassByte()));
                }
            }
            if (Environment.isDebug()) {
                System.out.printf("[ClassHotSwap.process] className:[%s], find class instance count: [%d]%n", request.getClassName(), needRedefineClass.size());
            }
            // 重新定义类，完成热加载
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
