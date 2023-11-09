package six.eared.macaque.agent.compiler.java;

import com.sun.tools.javac.file.JavacFileManager;
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
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

public class JavaSourceCompiler implements Compiler {

    private static final Set<String> CLASS_PATH_ROOTS = new HashSet<>();

    private static final Set<String> JAR_LIBRARIES = new HashSet<>();

    private static JavaSourceCompiler INSTANCE = null;

    private final JavaCompiler compiler;

    private final StandardJavaFileManager baseFileManager;

    static {
        String[] jars = System.getProperty("java.class.path").split(File.pathSeparator);
        for (String jarPath : jars) {
            try (JarFile jarFile = new JarFile(jarPath)) {
                Manifest manifest = jarFile.getManifest();
                if (manifest == null) {
                    continue;
                }
                String classpath = manifest.getMainAttributes().getValue(new Attributes.Name("Class-Path"));
                if (StringUtil.isNotEmpty(classpath)) {
                    for (StringTokenizer st = new StringTokenizer(classpath); st.hasMoreTokens(); ) {
                        String elt = st.nextToken();
                        if (elt.startsWith("file:/")) elt = elt.substring(6);
                        if (elt.endsWith(".jar") || elt.endsWith(".zip")) {
                            JAR_LIBRARIES.add(elt);
                            continue;
                        }
                        CLASS_PATH_ROOTS.add(elt);
                    }
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private JavaSourceCompiler() {
        this.compiler = ToolProvider.getSystemJavaCompiler();
        if (this.compiler == null) {
            if (Environment.isDebug()) {
                System.out.println("[JavaSourceCompiler] ToolProvider.getSystemJavaCompiler() is null, Jdk environment exception");
            }
        }
        this.baseFileManager = this.compiler == null ? null : this.compiler.getStandardFileManager(null, null, null);
    }

    public static JavaSourceCompiler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new JavaSourceCompiler();
            try {
                JavacFileManager javac = (JavacFileManager) INSTANCE.baseFileManager;
                javac.setLocation(StandardLocation.CLASS_PATH, JAR_LIBRARIES.stream().map(item -> new File(item)).collect(Collectors.toList()));
            } catch (IOException e) {

            }
        }
        return INSTANCE;
    }

    /**
     * @param sourceCodes key: JavaFileName,
     *                    value: javaCode
     * @return
     */
    @Override
    public List<byte[]> compile(Map<String, byte[]> sourceCodes) throws MemoryCompileException {
        List<JavaFileObject> javaFileObjects = new ArrayList<>();

        Set<Map.Entry<String, byte[]>> entries = sourceCodes.entrySet();
        for (Map.Entry<String, byte[]> entry : entries) {
            String fileName = entry.getKey();
            byte[] bytes = entry.getValue();
            JavaSourceFileObject sourceFile = new JavaSourceFileObject(FileUtil.createTmpFile(fileName, bytes));
            javaFileObjects.add(sourceFile);
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
            sbuilder.append("   ").append("class: " + errorEntry.getKey()).append("\n");
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
