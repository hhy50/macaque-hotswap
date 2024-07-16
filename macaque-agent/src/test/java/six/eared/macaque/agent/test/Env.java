package six.eared.macaque.agent.test;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.compiler.java.JavaSourceCompiler;
import six.eared.macaque.agent.javassist.JavaSsistUtil;
import six.eared.macaque.common.jps.PID;
import six.eared.macaque.common.util.FileUtil;
import six.eared.macaque.common.util.ReflectUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Env {
    static final Map<String, Class> PRELOADED = new HashMap<>();

    static {
        URL resource = Env.class.getClassLoader().getResource("macaque-agent-lightweight.jar");
        try {
            VirtualMachine attach = VirtualMachine.attach(String.valueOf(PID.getCurrentPid()));
            attach.loadAgent(new File(resource.toURI()).getPath(), "");
            attach.detach();
        } catch (AttachNotSupportedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (AgentLoadException e) {
            throw new RuntimeException(e);
        } catch (AgentInitializationException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        try {
            Enumeration<URL> preloads = Env.class.getClassLoader().getResources("preload");
            while (preloads.hasMoreElements()) {
                URL clazzUrl = preloads.nextElement();
                File preloadDir = new File(clazzUrl.toURI());
                if (!preloadDir.exists()) {
                    break;
                }
                for (String item : preloadDir.list((dir, name) -> name.endsWith(".java"))) {
                    for (byte[] clazzData : compileToClass(item, FileUtil.readBytes(preloadDir.getPath() + File.separator + item))) {
                        ClazzDefinition clazzDefinition = AsmUtil.readClass(clazzData);
                        Class<?> clazz = (Class<?>) ReflectUtil.invokeMethod(ClassLoader.getSystemClassLoader(),
                                "defineClass", clazzDefinition.getClassName(), clazzData, 0, clazzData.length);
                        try (InputStream arrayIn = new ByteArrayInputStream(clazzData)) {
                            JavaSsistUtil.POOL.makeClass(arrayIn);
                        }
                        PRELOADED.put(clazzDefinition.getClassName(), clazz);
                        System.out.printf("preload class: %s\n", clazz.getName());
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<byte[]> compileToClass(String fileName, byte[] sourceCode) {
        JavaSourceCompiler javaSourceCompiler = JavaSourceCompiler.getInstance();
        Map<String, byte[]> javaSource = new HashMap<>();
        javaSource.put(fileName, sourceCode);
        return javaSourceCompiler.compile(javaSource);
    }

    public static Object newInstance(String className, Object... args) {
        Class<?> clazz = PRELOADED.get(className);
        try {
            return ReflectUtil.newInstance(clazz, args);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public Class<?> getPreload(String s) {
        return PRELOADED.get(s);
    }

    public static Object invoke(Object obj, String methodName, Object... args) {
        return ReflectUtil.invokeMethod(obj, methodName, args);
    }

    public static Object invokeStatic(Class<?> target, String methodName, Object... args) {
        return ReflectUtil.invokeStaticMethod(target, methodName, args);
    }
}
