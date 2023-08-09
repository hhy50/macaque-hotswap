package six.eared.macaque.agent.compiler.java;


import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DynamicJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {

    private final Map<String, JavaFileObject> byteCodes = new HashMap<>();


    /**
     * Creates a new instance of ForwardingJavaFileManager.
     *
     * @param fileManager delegate to this file manager
     */
    public DynamicJavaFileManager(JavaFileManager fileManager) {
        super(fileManager);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location, String className,
                                               JavaFileObject.Kind kind, FileObject sibling) throws IOException {

        JavaFileObject javaFileObject = byteCodes.get(className);
        if (javaFileObject != null) {
            return javaFileObject;
        }
        javaFileObject = new ByteCodeOutStream(className);
//        javaFileObject = super.getJavaFileForOutput(location, className, kind, sibling);
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
}
