package six.eared.macaque.agent.compiler.java;

import six.eared.macaque.agent.compiler.Compiler;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.agent.exceptions.MemoryCompileException;
import six.eared.macaque.common.util.CollectionUtil;
import six.eared.macaque.common.util.FileUtil;
import six.eared.macaque.common.util.InstrumentationUtil;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

public class JavaSourceCompiler implements Compiler {

    private final Set<SearchRoot> classPathRoots = new HashSet<>();

    private static JavaSourceCompiler INSTANCE = null;

    private JavaCompiler compiler;

    private StandardJavaFileManager baseFileManager;

    private JavaSourceCompiler() throws IOException {
        this.compiler = ToolProvider.getSystemJavaCompiler();
        if (this.compiler == null) {
            if (Environment.isDebug()) {
                System.out.println("[JavaSourceCompiler] ToolProvider.getSystemJavaCompiler() is null, Jdk environment exception");
            }
            return;
        }
        this.baseFileManager = this.compiler.getStandardFileManager(null, null, null);
        initClasspathEnv();
    }

    private void initClasspathEnv() throws IOException {
        String userDir = System.getProperty("user.dir");
        String[] classSearchPaths = System.getProperty("java.class.path").split(File.pathSeparator);

        Set<File> jarSearchPathSet = new HashSet<>();
        for (String classSearchPath : classSearchPaths) {
            if (!isJarFile(classSearchPath)) {
                this.classPathRoots.add(new ClasspathSearchRoot(classSearchPath));
                continue;
            }
            File jarAbsolutePath = getJarAbsolutePath(classSearchPath, userDir);
            if (jarAbsolutePath == null) continue;
            try (PathJarFile jarFile = new PathJarFile(jarAbsolutePath)) {
                jarSearchPathSet.add(jarAbsolutePath);

                Manifest manifest = jarFile.getManifest();
                if (manifest != null) {
                    String classpath = manifest.getMainAttributes().getValue(new Attributes.Name("Class-Path"));
                    if (Objects.nonNull(classpath)) {
                        inner:
                        for (StringTokenizer st = new StringTokenizer(classpath); st.hasMoreTokens(); ) {
                            String ele = st.nextToken();
                            if (ele.startsWith("file:")) ele = ele.substring(5);
                            if (isJarFile(ele)) {
                                File absolutePath = getJarAbsolutePath(ele, userDir);
                                if (absolutePath != null) jarSearchPathSet.add(absolutePath);
                                continue inner;
                            }
                            this.classPathRoots.add(new ClasspathSearchRoot(ele));
                        }
                    }

                    // springboot的jar
                    String startClass = manifest.getMainAttributes().getValue(new Attributes.Name("Start-Class"));
                    if (Objects.nonNull(startClass)) {
                        Set<Class<?>> loadedClass = InstrumentationUtil.findLoadedClass(Environment.getInst(), startClass);
                        if (CollectionUtil.isNotEmpty(loadedClass)) {
                            for (Class<?> mainClass : loadedClass) {
                                this.classPathRoots.add(new ClassLoaderSearchRoot(mainClass.getClassLoader()));
                            }
                        }
                    }
                }
            }
        }
        this.baseFileManager.setLocation(StandardLocation.CLASS_PATH, jarSearchPathSet);
        this.baseFileManager.setLocation(StandardLocation.ANNOTATION_PROCESSOR_PATH, Collections.emptyList());
        if (Environment.isDebug()) {
            System.out.println("memory compiler load CLASS_PATH_ROOTS:" + this.classPathRoots);
            System.out.println("memory compiler load JAR_LIBRARIES:" + jarSearchPathSet);
        }
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
        try {
            return new JavaSourceCompiler();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
//            javaFileObjects.add(new JavaSourceStringObject(fileName, new String(bytes)));
        }
        DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();

        DynamicJavaFileManager fileManager = new DynamicJavaFileManager(baseFileManager, this.classPathRoots);
        JavaCompiler.CompilationTask task = this.compiler.getTask(null, fileManager, collector, buildOption(fileManager), null, javaFileObjects);
        boolean result = task.call();
        Map<String, List<Diagnostic<? extends JavaFileObject>>> errors = collector.getDiagnostics().stream()
                .filter(item -> item.getKind() == Diagnostic.Kind.ERROR)
                .collect(Collectors.groupingBy(diagnostic -> Optional.ofNullable(diagnostic.getSource().getName()).orElse("other")));
        if (!result || !errors.isEmpty()) {
            throw new MemoryCompileException(formatCompileErrorText(errors));
        }
        return fileManager.getByteCodes();
    }

    private List<String> buildOption(DynamicJavaFileManager fileManager) {
        List<String> options = new ArrayList<>();
        options.add("-Xlint:unchecked");
        options.add("-g");
        Set<String> annotationProcessor = null;
        try {
            annotationProcessor = fileManager.findAnnotationProcessor();
        } catch (IOException ignored) {}
        if (CollectionUtil.isNotEmpty(annotationProcessor)) {
            options.add("-processor");
            options.add(String.join(",", annotationProcessor));
        }
        return options;
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

    private static boolean isJarFile(String classpath) {
        return classpath.endsWith(".jar") || classpath.endsWith(".zip");
    }
}
