package six.eared.macaque.agent.compiler.java;

import six.eared.macaque.agent.compiler.Compiler;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.common.util.FileUtil;

import javax.tools.*;
import java.util.*;

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
    public List<byte[]> compile(Map<String, byte[]> sourceCodes) {
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

    public boolean isPrepare() {
        return this.compiler != null
                && this.baseFileManager != null;
    }
}
