package six.eared.macaque.agent.compiler.java;

import six.eared.macaque.agent.exceptions.CompileException;
import six.eared.macaque.common.util.CollectionUtil;
import six.eared.macaque.common.util.FileUtil;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class DynamicJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {

    private final Map<String, JavaFileObject> byteCodes = new HashMap<>();

    /**
     * classpath
     */
    private final Set<SearchRoot> classRootPath;

    /**
     * 注解处理器的搜索路径
     */
    private final Set<URL> processorPaths;

    public DynamicJavaFileManager(JavaFileManager fileManager, Set<SearchRoot> classRootPath) {
        super(fileManager);
        this.classRootPath = classRootPath != null ? new HashSet<>(classRootPath) : new HashSet<>();
        this.processorPaths = new HashSet<>();

        addProcessorPath(DynamicJavaFileManager.class.getProtectionDomain().getCodeSource().getLocation());
    }

    @Override
    public ClassLoader getClassLoader(Location location) {
        if (location == StandardLocation.ANNOTATION_PROCESSOR_PATH) {
            return new AnnotationProcessorClassloader(processorPaths.toArray(new URL[0]),
                    this.fileManager.getClass().getClassLoader());
        }
        return ClassLoader.getSystemClassLoader();
    }

    @Override
    public String inferBinaryName(Location location, JavaFileObject file) {
        if (file instanceof JavaSourceFileObject) {
            return ((JavaSourceFileObject) file).getName();
        }
        return super.inferBinaryName(location, file);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location, String className,
                                               JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        JavaFileObject javaFileObject = byteCodes.get(className);
        if (javaFileObject != null) {
            return javaFileObject;
        }
        javaFileObject = new ByteCodeOutStream(className);
        byteCodes.put(className, javaFileObject);
        return javaFileObject;
    }

    public List<byte[]> getByteCodes() {
        return byteCodes.values().stream()
                .map(item -> {
                    if (item instanceof ByteCodeOutStream) {
                        return ((ByteCodeOutStream) item).getByteCode();
                    }
                    try {
                        InputStream inputStream = item.openInputStream();
                        byte[] bytes = new byte[inputStream.available()];
                        inputStream.read(bytes, 0, bytes.length);
                        return bytes;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
        if (location == StandardLocation.PLATFORM_CLASS_PATH) {
            return super.list(location, packageName, kinds, recurse);
        }

        List<JavaFileObject> result = new ArrayList<>();
        if (location == StandardLocation.CLASS_PATH) {
            for (SearchRoot searchRoot : classRootPath) {
                result.addAll(searchRoot.search(packageName, kinds));
            }
        }
        for (JavaFileObject javaFileObject : super.list(location, packageName, kinds, recurse)) {
            result.add(javaFileObject);
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.classRootPath.forEach(SearchRoot::close);
    }

    public static JavaFileObject.Kind getKind(String var0) {
        if (var0.endsWith(JavaFileObject.Kind.CLASS.extension)) {
            return JavaFileObject.Kind.CLASS;
        } else if (var0.endsWith(JavaFileObject.Kind.SOURCE.extension)) {
            return JavaFileObject.Kind.SOURCE;
        } else {
            return var0.endsWith(JavaFileObject.Kind.HTML.extension) ? JavaFileObject.Kind.HTML : JavaFileObject.Kind.OTHER;
        }
    }

    public void addProcessorPath(URL processorPath) {
        File file = new File(processorPath.getPath());
        if (file.exists()) {
            this.processorPaths.add(processorPath);
            try {
                this.classRootPath.add(new ClassLoaderSearchRoot.JarFileIndex(processorPath.toExternalForm(), processorPath.toURI()));
            } catch (Exception e) {
                throw new CompileException(e);
            }
        }
    }

    public List<String> findAnnotationProcessor() throws IOException {
        List<String> processors = new ArrayList<>();

        if (CollectionUtil.isNotEmpty(this.processorPaths)) {
            ClassLoader apClassloader = getClassLoader(StandardLocation.ANNOTATION_PROCESSOR_PATH);
            Enumeration<URL> resources = apClassloader.getResources("META-INF/services/javax.annotation.processing.Processor");
            while (resources.hasMoreElements()) {
                try (InputStream in = resources.nextElement().openStream()) {
                    processors.addAll(Arrays.asList(new String(FileUtil.is2bytes(in)).split("\n")));
                }
            }
        }
        return processors;
    }
}
