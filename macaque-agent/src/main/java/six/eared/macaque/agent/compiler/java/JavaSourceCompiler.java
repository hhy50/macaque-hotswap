package six.eared.macaque.agent.compiler.java;

import six.eared.macaque.agent.compiler.Compiler;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.agent.exceptions.MemoryCompileException;
import six.eared.macaque.common.util.CollectionUtil;
import six.eared.macaque.common.util.FileUtil;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
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
            try (PathJarFile jarFile = new PathJarFile(getJarAbsolutePath(classSearchPath, userDir))) {
                Manifest manifest = jarFile.getManifest();
                if (manifest != null) {
                    String classpath = manifest.getMainAttributes().getValue(new Attributes.Name("Class-Path"));
                    if (Objects.nonNull(classpath)) {
                        inner:
                        for (StringTokenizer st = new StringTokenizer(classpath); st.hasMoreTokens(); ) {
                            String ele = st.nextToken();
                            if (ele.startsWith("file:/")) ele = ele.substring(6);
                            if (isJarFile(ele)) {
                                File absolutePath = getJarAbsolutePath(ele, userDir);
                                if (absolutePath != null) jarSearchPathSet.add(absolutePath);
                                continue inner;
                            }
                            this.classPathRoots.add(new ClasspathSearchRoot(ele));
                        }
                    }

                    // springboot的jar
                    String bootClasses = manifest.getMainAttributes().getValue(new Attributes.Name("Spring-Boot-Classes"));
                    String bootLibs = manifest.getMainAttributes().getValue(new Attributes.Name("Spring-Boot-Lib"));
                    if (Objects.nonNull(bootClasses) && Objects.nonNull(bootLibs)) {
                        PackageNameSearchRoot.loadBootJar(jarFile, bootClasses, bootLibs, this.classPathRoots);
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
        List<String> annotationProcessor = null;
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
