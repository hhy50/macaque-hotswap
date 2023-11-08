package six.eared.macaque.agent.compiler.java;


import six.eared.macaque.common.util.ClassUtil;
import six.eared.macaque.common.util.StringUtil;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

public class DynamicJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {
    private static final List<String> JAR_LIBRARIES = new ArrayList<>();

    private static final List<String> CLASS_PATH_ROOTS = new ArrayList<>();

    private final Map<String, JavaFileObject> byteCodes = new HashMap<>();

    public DynamicJavaFileManager(JavaFileManager fileManager) {
        super(fileManager);

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
                        if (!elt.endsWith(".jar")) {
                            CLASS_PATH_ROOTS.add(elt);
                            continue;
                        }
                        JAR_LIBRARIES.add(elt);
                    }
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }

    @Override
    public String inferBinaryName(Location location, JavaFileObject file) {
        if (file instanceof JavaClassFileObject) {
            return ((JavaClassFileObject) file).getClassName();
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
            for (String root : CLASS_PATH_ROOTS) {
                File packageFile = new File(root, ClassUtil.simpleClassName2path(packageName));
                if (packageFile.exists() && packageFile.isDirectory()) {
                    for (File classFile : packageFile.listFiles(item -> !item.isDirectory()
                            && item.getName().endsWith(".class"))) {
                        result.add(new JavaClassFileObject(classFile));
                    }
                }
            }
        }
        for (JavaFileObject javaFileObject : super.list(location, packageName, kinds, recurse)) {
            result.add(javaFileObject);
        }
        return result;
    }

    private static List<String> getPathEntries(String classPath) {
        List<String> entries = new ArrayList<>();
        int start = 0;
        while (start <= classPath.length()) {
            int sep = classPath.indexOf(File.pathSeparatorChar, start);
            if (sep == -1)
                sep = classPath.length();
            if (start < sep)
                entries.add(classPath.substring(start, sep));
            start = sep + 1;
        }
        return entries;
    }

    public static void main(String[] args) {
        List<String> jarLibraries = new ArrayList<>();
        List<String> classPathRoots = new ArrayList<>();

//        String classPath = System.getProperty("java.class.path");
//        String[] jars = {"C:\\Users\\49168\\Desktop\\classpath69898995.jar"};
//        for (String jarPath : jars) {
//            try (JarFile jarFile = new JarFile(jarPath)) {
//                Manifest manifest = jarFile.getManifest();
//                if (manifest == null) {
//                    continue;
//                }
//                String classpath = manifest.getMainAttributes().getValue(new Attributes.Name("Class-Path"));
//                if (StringUtil.isNotEmpty(classpath)) {
//                    for (StringTokenizer st = new StringTokenizer(classpath); st.hasMoreTokens(); ) {
//                        String elt = st.nextToken();
//                        if (!elt.endsWith(".jar")) {
//                            CLASS_PATH_ROOTS.add(elt);
//                            continue;
//                        }
//                        if (elt.startsWith("file:/")) elt = elt.substring(6);
//                        JAR_LIBRARIES.add(elt);
//                    }
//                }
//            } catch (IOException e) {
//                // ignore
//            }
//        }
        System.out.println(1);
    }
}
