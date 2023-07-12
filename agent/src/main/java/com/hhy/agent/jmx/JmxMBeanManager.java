package com.hhy.agent.jmx;


import com.hhy.agent.jmx.mbeans.ClassHotSwap;
import com.hhy.common.mbean.MBean;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
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

        // TODO scan package
        List<Class<MBean>> classes = loadMBeanClass();

        // registerMBean
        for (Class<MBean> clazz : classes) {
            MBean mBean = createMBean(clazz);
            try {
                mBeanServer.registerMBean(mBean, mBean.getMBeanName());
            } catch (Exception e) {

            }
        }
    }

    private List<Class<MBean>> loadMBeanClass() {
        return (List) List.of(
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

        }
        return null;
    }
}
