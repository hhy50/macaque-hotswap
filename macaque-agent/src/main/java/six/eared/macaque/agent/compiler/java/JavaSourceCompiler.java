package six.eared.macaque.agent.compiler.java;

import six.eared.macaque.agent.compiler.Compiler;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.common.util.FileUtil;

import javax.tools.*;
import java.util.*;

public class JavaSourceCompiler implements Compiler {

    private final JavaCompiler compiler;

    private StandardJavaFileManager baseFileManager;

    public JavaSourceCompiler() {
        this.compiler = ToolProvider.getSystemJavaCompiler();
        if (this.compiler == null) {
            throw new RuntimeException("ToolProvider.getSystemJavaCompiler() is null, jdk environment exception");
        }
        this.baseFileManager = this.compiler.getStandardFileManager(null, null, null);
    }

    /**
     *
     * @param sourceCodes key: JavaFileName,
     *                    value: javaCode
     * @return
     */
    @Override
    public List<byte[]> compile(Map<String, String> sourceCodes) {
        List<JavaFileObject> javaFileObjects = new ArrayList<>();

        for (String fileName : sourceCodes.keySet()) {
            javaFileObjects.add(new JavaSourceFileObject(
                    FileUtil.createTmpFile(fileName, sourceCodes.get(fileName).getBytes())));
        }

        DynamicJavaFileManager fileManager = new DynamicJavaFileManager(baseFileManager);
        DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();

        JavaCompiler.CompilationTask task = this.compiler.getTask(null, fileManager, collector,
                Arrays.asList("-Xlint:unchecked"), null, javaFileObjects);

        boolean result = task.call();
        if (!result || collector.getDiagnostics().size() > 0) {
            if (Environment.isDebug()) {
                for (Diagnostic<? extends JavaFileObject> diagnostic : collector.getDiagnostics()) {
                    System.out.println("line: " + diagnostic.getLineNumber() + ", message: " + diagnostic.getMessage(Locale.US));
                }
            }
            return null;
        }
        return fileManager.getByteCodes();
    }
}
