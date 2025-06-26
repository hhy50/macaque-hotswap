package six.eared.macaque.agent.compiler.java;

import lombok.Getter;
import six.eared.macaque.common.util.CollectionUtil;

import javax.tools.*;
import java.io.IOException;
import java.io.InputStream;
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
    @Getter
    private final Map<String, ClassLoader> processors;

    public DynamicJavaFileManager(JavaFileManager fileManager, Set<SearchRoot> classRootPath) {
        super(fileManager);
        this.classRootPath = classRootPath != null ? new HashSet<>(classRootPath) : new HashSet<>();
        this.processors = findAnnotationProcessor();
    }

    @Override
    public ClassLoader getClassLoader(Location location) {
        if (location == StandardLocation.ANNOTATION_PROCESSOR_PATH) {
            return new AnnotationProcessorClassloader(new HashSet<>(this.processors.values()));
        }
        return ClassLoader.getSystemClassLoader();
    }

    @Override
    public String inferBinaryName(Location location, JavaFileObject file) {
        if (file instanceof JavaClassFileObject) {
            return ((JavaClassFileObject) file).getName();
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

    public Map<String, ClassLoader> findAnnotationProcessor() {
        Map<String, ClassLoader> processors = new HashMap<>();

        if (CollectionUtil.isNotEmpty(this.classRootPath)) {
            for (SearchRoot searchRoot : this.classRootPath) {
                processors.putAll(searchRoot.searchAnnotationProcessors());
            }
        }
        return processors;
    }
}
