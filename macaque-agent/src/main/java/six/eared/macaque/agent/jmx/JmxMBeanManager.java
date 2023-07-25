package six.eared.macaque.agent.jmx;


import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.agent.jmx.mbeans.ClassHotSwap;
import six.eared.macaque.agent.jmx.mbeans.JmxHeartbeat;
import six.eared.macaque.mbean.MBean;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;

public class JmxMBeanManager {

    private final MBeanServer mBeanServer;

    public JmxMBeanManager() {
        this.mBeanServer = ManagementFactory.getPlatformMBeanServer();

        registerAllMBean();
    }

    public MBeanServer getMBeanServer() {
        return mBeanServer;
    }

    public void registerAllMBean() {

        // TODO: scan package
        List<Class<MBean>> classes = loadMBeanClass();

        // registerMBean
        for (Class<MBean> clazz : classes) {
            MBean mBean = createMBean(clazz);
            try {
                mBeanServer.registerMBean(mBean, mBean.getMBeanName());
            } catch (Exception e) {
                if (Environment.isDebug()) {
                    System.out.println("registerMBean error");
                    e.printStackTrace();
                }
            }
        }
    }

    private List<Class<MBean>> loadMBeanClass() {
        return (List) Arrays.asList(
                JmxHeartbeat.class,
                ClassHotSwap.class);
    }


    /**
     * 创建mbean
     *
     * @return MBeanInstance
     */
    public MBean createMBean(Class<MBean> mBeanClass) {
        try {
            MBean mBean = mBeanClass.newInstance();
            return mBean;
        } catch (Exception e) {
            if (Environment.isDebug()) {
                System.out.println("createMBean error");
                e.printStackTrace();
            }
        }
        return null;
    }
}
