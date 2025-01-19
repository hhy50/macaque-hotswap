package six.eared.macaque.agent.jmx;


import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.agent.jmx.mbeans.HotSwap;
import six.eared.macaque.agent.jmx.mbeans.JmxHeartbeat;
import six.eared.macaque.mbean.MBean;
import six.eared.macaque.mbean.rmi.RmiData;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;

/**
 * JMX MBean 管理器
 */
public class JmxMBeanManager {

    /**
     * MBeanServer 实例
     * 用于管理MBean的生命周期，注册MBean，注销MBean等
     */
    private final MBeanServer mBeanServer;

    public JmxMBeanManager() {
        // 获取MBeanServer，如果不存在则创建，一般情况下，JVM中只有一个MBeanServer，所以这里直接获取。
        this.mBeanServer = ManagementFactory.getPlatformMBeanServer();
    }

    public MBeanServer getMBeanServer() {
        return mBeanServer;
    }

    /**
     * 注册所有MBean
     */
    public void registerAllMBean() {

        // TODO: scan package
        List<Class<? extends MBean<? extends RmiData>>> classes = loadMBeanClass();

        // registerMBean
        for (Class<? extends MBean<? extends RmiData>> clazz : classes) {
            // 创建MBean实例
            MBean<? extends RmiData> mBean = createMBean(clazz);
            try {
                // 注册MBean
                mBeanServer.registerMBean(mBean, mBean.getMBeanName());
            } catch (Exception e) {
                if (Environment.isDebug()) {
                    System.out.println("registerMBean error");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 所有需要加载的MBean类
     *
     * @return 需要加载的MBean类
     */
    private List<Class<? extends MBean<? extends RmiData>>> loadMBeanClass() {
        return Arrays.asList(
                JmxHeartbeat.class,
                HotSwap.class);
    }


    /**
     * 创建mbean
     *
     * @return MBean实例
     */
    public MBean<? extends RmiData> createMBean(Class<? extends MBean<? extends RmiData>> mBeanClass) {
        try {
            return mBeanClass.newInstance();
        } catch (Exception e) {
            if (Environment.isDebug()) {
                System.out.println("createMBean error");
                e.printStackTrace();
            }
        }
        return null;
    }
}
