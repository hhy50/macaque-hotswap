package six.eared.macaque.agent;

import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.agent.jmx.JmxMBeanManager;

import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import java.io.ByteArrayInputStream;
import java.lang.instrument.Instrumentation;
import java.rmi.registry.LocateRegistry;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 探针引导类
 */
public class AgentBootstrap {

    /**
     * 启动标记
     */
    private static final AtomicBoolean START_FLAG = new AtomicBoolean(false);

    /**
     * JMX MBean 管理器
     */
    public static JmxMBeanManager JMX_MBEAN_MANAGER;

    /**
     * 启动探针
     * 此方法在{@link six.eared.macaque.agent.AgentMain}类中loadBootstrap(String, Instrumentation)方法通过反射调用
     *
     * @param args        参数
     * @param inst        inst
     * @return 是否启动成功
     */
    public static Boolean start(String args, Instrumentation inst) {
        if (!START_FLAG.get()) {
            Properties properties = parseArgs(args);
            try {
                boolean debug = Boolean.parseBoolean(properties.getProperty("debug", "false"));
                int jmxPort = Integer.parseInt(properties.getProperty("port", "3030"));

                // init env
                Environment.initEnv(debug, inst);

                // init jmx, mbeans
                JMX_MBEAN_MANAGER = initJmxService(jmxPort);

                //启动完成
                START_FLAG.set(true);
                System.out.printf("attach success, jmx port=%d\n",
                        jmxPort);
                return true;
            } catch (Exception e) {
                if (Environment.isDebug()) {
                    System.out.println("start error");
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 初始化JMX服务
     *
     * @param port 端口
     * @return JMX MBean 管理器
     */
    private static JmxMBeanManager initJmxService(int port) {
        try {
            JmxMBeanManager jmxMBeanManager = new JmxMBeanManager();
            //在指定端口创建Registry
            LocateRegistry.createRegistry(port);
            //JMX API连接器服务地址，端口号可任意指定，但需与上面创建的Registry端口一致
            //该地址是SLP（服务定位协议）的抽象地址
            JMXServiceURL url = new JMXServiceURL(String.format("service:jmx:rmi:///jndi/rmi://127.0.0.1:%d/macaque", port));
            JMXConnectorServer jcs = JMXConnectorServerFactory.newJMXConnectorServer(url,
                    null, jmxMBeanManager.getMBeanServer());
            jcs.start();

            return jmxMBeanManager;
        } catch (Exception e) {
            if (Environment.isDebug()) {
                System.out.println("initJmxService error");
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 解析参数
     *
     * @param args 参数
     * @return 参数
     */
    private static Properties parseArgs(String args) {
        try {
            if (args != null && args.length() > 0) {
                String s = args.replaceAll("\\s+", "")
                        .replaceAll(",", "\n");
                Properties properties = new Properties();
                properties.load(new ByteArrayInputStream(s.getBytes()));
                return properties;
            }
        } catch (Exception e) {
            if (Environment.isDebug()) {
                System.out.println("parseArgs error");
                e.printStackTrace();
            }
        }
        return new Properties();
    }
}
