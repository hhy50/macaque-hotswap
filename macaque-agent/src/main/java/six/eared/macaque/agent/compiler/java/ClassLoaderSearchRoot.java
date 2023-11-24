package six.eared.macaque.agent.compiler.java;

import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class ClassLoaderSearchRoot implements SearchRoot {
    private final ClassLoader classLoader;

    private static final String CLASS_FILE_EXTENSION = ".class";

    private static final Map<String, JarFileIndex> INDEXS = new ConcurrentHashMap<>();

    public ClassLoaderSearchRoot(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public List<JavaFileObject> search(String packageName, Set<JavaFileObject.Kind> kinds) throws IOException {
        String javaPackageName = packageName.replaceAll("\\.", "/");

        List<JavaFileObject> result = new ArrayList<>();
        Enumeration<URL> urlEnumeration = classLoader.getResources(javaPackageName);
        while (urlEnumeration.hasMoreElements()) {
            URL packageFolderURL = urlEnumeration.nextElement();
            result.addAll(listUnder(packageName, packageFolderURL));
        }
        return result;
    }

    private Collection<JavaFileObject> listUnder(String packageName, URL packageFolderURL) {
        File directory = new File(decode(packageFolderURL.getFile()));
        if (directory.isDirectory()) {
            return processDir(packageName, directory);
        } else {
            return processJar(packageName, packageFolderURL);
        }
    }

    private List<JavaFileObject> processJar(String packageName, URL packageFolderURL) {
        try {
            String jarUri = packageFolderURL.toExternalForm().substring(0, packageFolderURL.toExternalForm().lastIndexOf("!/"));
            JarFileIndex jarFileIndex = INDEXS.get(jarUri);
            if (jarFileIndex == null) {
                jarFileIndex = new JarFileIndex(jarUri, URI.create(jarUri + "!/"));
                INDEXS.put(jarUri, jarFileIndex);
            }
            List<JavaFileObject> result = jarFileIndex.search(packageName, EnumSet.of(JavaFileObject.Kind.CLASS));
            if (result != null) {
                return result;
            }
        } catch (Exception e) {
            // ignore
        }
        return fuse(packageFolderURL);
    }

    private List<JavaFileObject> fuse(URL packageFolderURL) {
        List<JavaFileObject> result = new ArrayList<>();
        try {
            String jarUri = packageFolderURL.toExternalForm().substring(0, packageFolderURL.toExternalForm().lastIndexOf("!/"));

            JarURLConnection jarConn = (JarURLConnection) packageFolderURL.openConnection();
            String rootEntryName = jarConn.getEntryName();
            int rootEnd = rootEntryName.length() + 1;

            Enumeration<JarEntry> entryEnum = jarConn.getJarFile().entries();
            while (entryEnum.hasMoreElements()) {
                JarEntry jarEntry = entryEnum.nextElement();
                String name = jarEntry.getName();
                if (name.startsWith(rootEntryName) && name.indexOf('/', rootEnd) == -1 && name.endsWith(CLASS_FILE_EXTENSION)) {
                    URI uri = URI.create(jarUri + "!/" + name);
                    String binaryName = name.replaceAll("/", ".");
                    binaryName = binaryName.replaceAll(CLASS_FILE_EXTENSION + "$", "");

                    result.add(new JavaSourceFileObject(uri, binaryName, JavaFileObject.Kind.CLASS));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Wasn't able to open " + packageFolderURL + " as a jar file", e);
        }
        return result;
    }

    private List<JavaFileObject> processDir(String packageName, File directory) {
        File[] files = directory.listFiles(item ->
                item.isFile() && DynamicJavaFileManager.getKind(item.getName()) == JavaFileObject.Kind.CLASS);
        if (files != null) {
            return Arrays.stream(files).map(item -> {
                String className = packageName + "." + item.getName()
                        .replaceAll(CLASS_FILE_EXTENSION + "$", "");
                return new JavaSourceFileObject(item.toURI(), className, JavaFileObject.Kind.CLASS);
            }).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private String decode(String filePath) {
        try {
            return URLDecoder.decode(filePath, "utf-8");
        } catch (Exception e) {
            // ignore, return original string
        }

        return filePath;
    }

    public static class JarFileIndex implements SearchRoot {
        private String jarUri;
        private URI uri;

        private Map<String, List<ClassUriWrapper>> packages = new HashMap<>();

        public JarFileIndex(String jarUri, URI uri) throws IOException {
            this.jarUri = jarUri;
            this.uri = uri;
            loadIndex();
        }

        private void loadIndex() throws IOException {
            URLConnection jarConn = uri.toURL().openConnection();

            String rootEntryName = "";
            JarFile jarFile = null;
            if (jarConn instanceof JarURLConnection) {
                rootEntryName = Optional.ofNullable(((JarURLConnection) jarConn).getEntryName()).orElse("");
                jarFile = ((JarURLConnection) jarConn).getJarFile();
            } else {
                jarFile = new JarFile(new File(uri));
                this.jarUri = "jar:" + this.jarUri;
            }
            try {
                Enumeration<JarEntry> entriesIt = jarFile.entries();
                while (entriesIt.hasMoreElements()) {
                    JarEntry jarEntry = entriesIt.nextElement();
                    String entryName = jarEntry.getName();
                    if (entryName.startsWith(rootEntryName) && entryName.endsWith(CLASS_FILE_EXTENSION)) {
                        String className = entryName
                                .substring(0, entryName.length() - CLASS_FILE_EXTENSION.length())
                                .replace(rootEntryName, "")
                                .replace("/", ".");
                        if (className.startsWith(".")) className = className.substring(1);
                        if (className.equals("package-info")
                                || className.equals("module-info")
                                || className.lastIndexOf(".") == -1) {
                            continue;
                        }
                        String packageName = className.substring(0, className.lastIndexOf("."));
                        List<ClassUriWrapper> classes = packages.get(packageName);
                        if (classes == null) {
                            classes = new ArrayList<>();
                            packages.put(packageName, classes);
                        }
                        classes.add(new ClassUriWrapper(className, URI.create(jarUri + "!/" + entryName)));
                    }
                }
            } finally {
                if (jarFile != null) jarFile.close();
            }
        }

        public List<JavaFileObject> search(String packageName, Set<JavaFileObject.Kind> kinds) {
            if (this.packages.isEmpty()) {
                return null;
            }
            if (this.packages.containsKey(packageName)) {
                return packages.get(packageName).stream().map(item -> {
                    return new JavaSourceFileObject(item.getUri(), item.getClassName(), JavaFileObject.Kind.CLASS);
                }).collect(Collectors.toList());
            }
            return Collections.emptyList();
        }
    }
}
