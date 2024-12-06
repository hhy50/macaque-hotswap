package six.eared.macaque.agent.test;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import io.github.hhy50.linker.LinkerFactory;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.compiler.java.JavaSourceCompiler;
import six.eared.macaque.agent.enhance.ClazzDataDefinition;
import six.eared.macaque.common.ExtPropertyName;
import six.eared.macaque.common.jps.PID;
import six.eared.macaque.common.util.FileUtil;
import six.eared.macaque.common.util.ReflectUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Env {
    static final Map<String, Class> PRELOADED = new HashMap<>();

    static {
        attach();
        setEnv();
        preload();
    }

    private static void setEnv() {
        LinkerFactory.setOutputPath(FileUtil.getProcessTmpPath()+"/linker-output");
    }

    private static void attach() {
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
    }

    public static void preload() {
        try {
            Enumeration<URL> preloads = Env.class.getClassLoader().getResources("preload");
            while (preloads.hasMoreElements()) {
                URL clazzUrl = preloads.nextElement();
                File preloadDir = new File(clazzUrl.toURI());
                if (!preloadDir.exists()) {
                    break;
                }
                Map<String, byte[]> javaSources = new HashMap<>();
                for (String item : preloadDir.list((dir, name) -> name.endsWith(".java"))) {
                    javaSources.put(item, FileUtil.readFile(preloadDir.getPath()+File.separator+item));
                }
                Map<String, ClazzDataDefinition> definitions = compileToClass(javaSources).stream().map(AsmUtil::readClass)
                        .collect(Collectors.toMap(ClazzDefinition::getClassName, Function.identity()));
                for (Map.Entry<String, ClazzDataDefinition> definitionEntry : definitions.entrySet()) {
                    loadClass(definitions, definitionEntry.getKey(), definitionEntry.getValue());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadClass(Map<String, ClazzDataDefinition> definitions, String className, ClazzDataDefinition definition) throws IOException {
        if (PRELOADED.containsKey(className)) return;
        if (definition.getSuperClassName() != null
                && !definition.getSuperClassName().equals("java.lang.Object")
                && definitions.containsKey(definition.getSuperClassName())) {
            loadClass(definitions, definition.getSuperClassName(), definitions.get(definition.getSuperClassName()));
        }

        byte[] bytecode = definition.getBytecode();
        Class<?> clazz = (Class<?>) ReflectUtil.invokeMethod(ClassLoader.getSystemClassLoader(),
                "defineClass", className, bytecode, 0, bytecode.length);
//        try (InputStream arrayIn = new ByteArrayInputStream(bytecode)) {
//            JavaSsistUtil.POOL.makeClass(arrayIn);
//        }
        PRELOADED.put(className, clazz);
        System.out.printf("preload class: %s\n", clazz.getName());
    }

    public static List<byte[]> compileToClass(String fileName, byte[] sourceCode) {
        JavaSourceCompiler javaSourceCompiler = JavaSourceCompiler.getInstance();
        Map<String, byte[]> javaSource = new HashMap<>();
        javaSource.put(fileName, sourceCode);
        return javaSourceCompiler.compile(javaSource);
    }

    public static List<byte[]> compileToClass(Map<String, byte[]> javaSources) {
        JavaSourceCompiler javaSourceCompiler = JavaSourceCompiler.getInstance();
        return javaSourceCompiler.compile(javaSources);
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

    public static Map<String, String> compatibilityMode() {
        Map<String, String> extProperties = new HashMap<>();
        extProperties.put(ExtPropertyName.COMPATIBILITY_MODE, Boolean.TRUE.toString());
        return extProperties;
    }
}
