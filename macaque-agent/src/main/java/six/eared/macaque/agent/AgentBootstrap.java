package six.eared.macaque.agent;

import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.agent.jmx.JmxMBeanManager;
import six.eared.macaque.agent.spi.LibrarySpiLoader;
import six.eared.macaque.common.util.FileUtil;

import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.rmi.registry.LocateRegistry;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class AgentBootstrap {

    private static final AtomicBoolean START_FLAG = new AtomicBoolean(false);

    /**
     * JMX MBean 管理器
     */
    public static JmxMBeanManager JMX_MBEAN_MANAGER;

    /**
     * 反射调用 {@link six.eared.macaque.agent.AgentMain#loadBootstrap}
     *
     * @param args 启动参数
     * @param inst 插桩对象
     * @return 启动结果
     */
    public static Boolean start(String args, Instrumentation inst) {
        if (START_FLAG.compareAndSet(false, true)) {
            Properties properties = parseArgs(args);
            try {
                boolean debug = Boolean.parseBoolean(properties.getProperty("debug", "false"));
                int jmxPort = Integer.parseInt(properties.getProperty("port", "3030"));

                // init env
                Environment.initEnv(debug, inst);

                // init jmx, mbeans
                JMX_MBEAN_MANAGER = initJmxService(jmxPort);

                // init Library
                LibrarySpiLoader.initLibrary();

                JMX_MBEAN_MANAGER.registerAllMBean();

                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        // 清理临时目录
                        FileUtil.deleteFile(new File(FileUtil.getProcessTmpPath()));
                    }
                });
                System.out.printf("attach success, jmx port=%d\n", jmxPort);
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

    private static JmxMBeanManager initJmxService(int port) throws IOException {
        JmxMBeanManager jmxMBeanManager = new JmxMBeanManager();
        LocateRegistry.createRegistry(port);
        JMXServiceURL url = new JMXServiceURL(String.format("service:jmx:rmi:///jndi/rmi://127.0.0.1:%d/macaque", port));
        JMXConnectorServer jcs = JMXConnectorServerFactory.newJMXConnectorServer(url,
                null, jmxMBeanManager.getMBeanServer());
        jcs.start();

        return jmxMBeanManager;
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
