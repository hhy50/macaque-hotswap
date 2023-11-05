package six.eared.macaque.agent.compiler.java;

import six.eared.macaque.agent.compiler.Compiler;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.agent.exceptions.MemoryCompileException;
import six.eared.macaque.common.util.FileUtil;

import javax.tools.*;
import java.util.*;
import java.util.stream.Collectors;

public class JavaSourceCompiler implements Compiler {

    private final JavaCompiler compiler;

    private final StandardJavaFileManager baseFileManager;

    public JavaSourceCompiler() {
        this.compiler = ToolProvider.getSystemJavaCompiler();
        if (this.compiler == null) {
            if (Environment.isDebug()) {
                System.out.println("[JavaSourceCompiler] ToolProvider.getSystemJavaCompiler() is null, Jdk environment exception");
            }
        }
        this.baseFileManager = this.compiler == null ? null : this.compiler.getStandardFileManager(null, null, null);
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

        DynamicJavaFileManager fileManager = new DynamicJavaFileManager(baseFileManager);
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
