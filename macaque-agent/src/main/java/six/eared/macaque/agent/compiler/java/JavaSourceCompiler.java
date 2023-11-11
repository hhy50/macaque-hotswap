package six.eared.macaque.agent.compiler.java;

import six.eared.macaque.agent.compiler.Compiler;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.agent.exceptions.MemoryCompileException;
import six.eared.macaque.common.util.FileUtil;
import six.eared.macaque.common.util.StringUtil;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

public class JavaSourceCompiler implements Compiler {

    private static final Set<String> CLASS_PATH_ROOTS = new HashSet<>();

    private static final Set<File> JAR_LIBRARIES = new HashSet<>();

    private static JavaSourceCompiler INSTANCE = null;

    private JavaCompiler compiler;

    private StandardJavaFileManager baseFileManager;

    static {
        String userDir = System.getProperty("user.dir");
//        String[] jars = System.getProperty("java.class.path").split(File.pathSeparator);
        String[] jars = {"C:\\Users\\49168\\Desktop\\billiards\\billiards-service-1.0-SNAPSHOT.jar"};
        for (String jarPath : jars) {
            try (JarFile jarFile = new JarFile(getJarAbsolutePath(jarPath, userDir))) {
                Manifest manifest = jarFile.getManifest();
                if (manifest == null) {
                    continue;
                }
                String classpath = manifest.getMainAttributes().getValue(new Attributes.Name("Class-Path"));
                if (StringUtil.isNotEmpty(classpath)) {
                    for (StringTokenizer st = new StringTokenizer(classpath); st.hasMoreTokens(); ) {
                        String ele = st.nextToken();
                        if (ele.startsWith("file:/")) ele = ele.substring(6);
                        if (ele.endsWith(".jar") || ele.endsWith(".zip")) {
                            File absolutePath = getJarAbsolutePath(ele, userDir);
                            if (absolutePath != null) JAR_LIBRARIES.add(absolutePath);
                            continue;
                        }
                        CLASS_PATH_ROOTS.add(ele);
                    }
                }
                String startClass = manifest.getMainAttributes().getValue(new Attributes.Name("Start-Class"));
                String bootClasses = manifest.getMainAttributes().getValue(new Attributes.Name("Spring-Boot-Classes"));
                String bootLib = manifest.getMainAttributes().getValue(new Attributes.Name("Spring-Boot-Lib"));
                if (StringUtil.isNotEmpty(startClass) && StringUtil.isNotEmpty(bootClasses)
                        && StringUtil.isNotEmpty(bootLib)) {
                    String tmpClasspath = Environment.getAndInitTmpClasspath();

                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String entryName = entry.getName();

                        if ((entryName.startsWith(bootClasses) || entryName.startsWith(bootLib))
                                && (entryName.endsWith(".class") || entryName.endsWith(".jar"))) {
                            File tmpFile = new File(tmpClasspath, entryName.replace(bootClasses, ""));
                            FileUtil.writeBytes(tmpFile, jarFile.getInputStream(entry));
                            if (entryName.endsWith(".jar")) {
                                JAR_LIBRARIES.add(tmpFile);
                            }
                        }
                    }
                    CLASS_PATH_ROOTS.add(tmpClasspath);
                }
            } catch (IOException e) {
                // ignore
            }
        }
        if (Environment.isDebug()) {
            System.out.println("memory compiler load CLASS_PATH_ROOTS:" + CLASS_PATH_ROOTS);
            System.out.println("memory compiler load JAR_LIBRARIES:" + JAR_LIBRARIES);
        }
    }

    private JavaSourceCompiler() throws IOException {
        this.compiler = ToolProvider.getSystemJavaCompiler();
        if (this.compiler == null) {
            if (Environment.isDebug()) {
                System.out.println("[JavaSourceCompiler] ToolProvider.getSystemJavaCompiler() is null, Jdk environment exception");
            }
            return;
        }
        this.baseFileManager = this.compiler.getStandardFileManager(null, null, null);
        this.baseFileManager.setLocation(StandardLocation.CLASS_PATH, JAR_LIBRARIES);
    }

    /**
     * 获取编译器
     *
     * @return
     */
    public static JavaSourceCompiler getInstance() {
        if (INSTANCE == null) {
            try {
                INSTANCE = new JavaSourceCompiler();
            } catch (IOException e) {
                if (Environment.isDebug()) {
                    e.printStackTrace();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 获取jar包相对路径
     *
     * @param jarFile
     * @param userDir
     * @return
     */
    private static File getJarAbsolutePath(String jarFile, String userDir) {
        File file = new File(jarFile);
        if (file.exists()) {
            return file;
        }
        file = new File(userDir, jarFile);
        if (file.exists()) {
            return file;
        }
        return null;
    }

    /**
     * @param sourceCodes key: JavaFileName,
     *                    value: javaCode
     * @return
     */
    @Override
    public List<byte[]> compile(Map<String, byte[]> sourceCodes) throws MemoryCompileException {
        if (Environment.isDebug()) {
            System.out.println("start compile java file: " + sourceCodes.keySet());
        }
        List<JavaFileObject> javaFileObjects = new ArrayList<>();

        Set<Map.Entry<String, byte[]>> entries = sourceCodes.entrySet();
        for (Map.Entry<String, byte[]> entry : entries) {
            String fileName = entry.getKey();
            byte[] bytes = entry.getValue();
            File tmpFile = FileUtil.createTmpFile("compile" + File.separator + fileName, bytes);
            javaFileObjects.add(new JavaSourceFileObject(tmpFile));
        }

        DynamicJavaFileManager fileManager = new DynamicJavaFileManager(baseFileManager, CLASS_PATH_ROOTS);
        DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();

        JavaCompiler.CompilationTask task = this.compiler.getTask(null, fileManager, collector,
                Collections.singleton("-Xlint:unchecked"), null, javaFileObjects);

        boolean result = task.call();
        Map<String, List<Diagnostic<? extends JavaFileObject>>> errors = collector.getDiagnostics().stream()
                .filter(item -> item.getKind() == Diagnostic.Kind.ERROR)
                .collect(Collectors.groupingBy(diagnostic -> diagnostic.getSource().getName()));
        if (!result || !errors.isEmpty()) {
            throw new MemoryCompileException(formatCompileErrorText(errors));
        }
        return fileManager.getByteCodes();
    }

    private String formatCompileErrorText(Map<String, List<Diagnostic<? extends JavaFileObject>>> errors) {
        StringBuilder sbuilder = new StringBuilder("compile error: \n");
        for (Map.Entry<String, List<Diagnostic<? extends JavaFileObject>>> errorEntry : errors.entrySet()) {
            sbuilder.append("   ").append("class: " + FileUtil.getFileName(errorEntry.getKey())).append("\n");
            sbuilder.append("   ").append(errorEntry.getValue().stream().map(diagnostic -> " line: " + diagnostic.getLineNumber() + ", message: " + diagnostic.getMessage(Locale.US))
                    .collect(Collectors.joining("\n     "))).append("\n");
        }
        return sbuilder.toString();
    }

    public boolean isPrepared() {
        return this.compiler != null
                && this.baseFileManager != null;
    }
}
