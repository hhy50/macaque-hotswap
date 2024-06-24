package six.eared.macaque.agent.loader;


import java.io.*;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;

/**
 * 探针主类
 */
public class AgentMain {

    private static final String BOOT_JAR = "lib/agent.jar";

    private static boolean START_FLAG = false;

    public static void agentmain(String args, Instrumentation inst) {
        loadBootstrap(args, inst);
    }

    /**
     * 加载引导类
     *
     * @param args 参数
     * @param inst inst
     */
    private synchronized static void loadBootstrap(String args, Instrumentation inst) {
        if (!START_FLAG) {
            Class<?> bootstrapClass = null;
            try {
                File agentJar = getAgentJar();
                if (agentJar == null) {
                    return;
                }
                // 加载引导类
                ClassLoader classLoader = getClassLoader(agentJar.toURI().toURL());
                bootstrapClass = classLoader.loadClass("six.eared.macaque.agent.AgentBootstrap");
            } catch (Exception e) {
                System.out.println("load AgentBootstrap.class error");
                e.printStackTrace();
            }

            if (bootstrapClass != null) {
                try {
                    // 调用引导类的start方法
                    Method init = bootstrapClass.getMethod("start", String.class, Instrumentation.class);
                    START_FLAG = (boolean) init.invoke(null, args, inst);
                } catch (Exception e) {
                    System.out.println("AgentBootstrap.start error");
                    e.printStackTrace();
                }
            }
        }
    }

    private static File getAgentJar() throws IOException {
        File file = new File(System.getProperty("java.io.tmpdir"), "macaque-agent.jar");
        try (InputStream in = AgentMain.class.getClassLoader().getResourceAsStream(BOOT_JAR);
             OutputStream out = new FileOutputStream(file);) {
            byte[] buffer = new byte[4 * 1024];
            int bytesRead = -1;
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        }
        return file;
    }

    /**
     * 获取系统类加载器
     *
     * @return 类加载器
     */

    private static ClassLoader getClassLoader(URL agentJar) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        while (classLoader.getParent() != null) {
            classLoader = classLoader.getParent();
        }
        return new MacaqueClassLoader(agentJar, classLoader);
    }

    public static void main(String[] args) throws IOException {
        // 加载agent.jar
        URL agentJar = new URL("jar:file:/C:/Users/49168/IdeaProjects/macaque-hotswap/macaque-server/build/distributions/bin/macaque-agent.jar!/lib/agent.jar");
        if (agentJar == null) {
            return;
        }
        ClassLoader classLoader = getClassLoader(agentJar);
        Class<?> bootstrapClass = null;
        try {
            // 加载引导类
            bootstrapClass = classLoader.loadClass("six.eared.macaque.agent.AgentBootstrap");
        } catch (Exception e) {
            System.out.println("load AgentBootstrap.class error");
            e.printStackTrace();
        }
    }
}
