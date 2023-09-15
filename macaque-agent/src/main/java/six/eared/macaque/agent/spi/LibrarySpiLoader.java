package six.eared.macaque.agent.spi;

import six.eared.macaque.agent.hotswap.handler.FileHookHandler;
import six.eared.macaque.common.util.ReflectUtil;
import six.eared.macaque.library.annotation.Library;
import six.eared.macaque.library.hook.HotswapHook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class LibrarySpiLoader {

    private static final String PATH = "META-INF/Library/";

    public static <T> Iterator<T> loadService(Class<T> clazz) {
        ServiceLoader<T> loader = ServiceLoader.load(clazz);
        return loader.iterator();
    }

    public static void initLibrary() {
        for (LibraryDefinition library : findLibrary()) {
            Library libraryAnnotation = library.getClazz().getAnnotation(Library.class);
            if (libraryAnnotation != null) {
                for (Class<? extends HotswapHook> hook : libraryAnnotation.hooks()) {
                    FileHookHandler.registerHook(ReflectUtil.newInstance(hook));
                }
            }
        }
    }

    public static List<LibraryDefinition> findLibrary() {
        List<LibraryDefinition> libraries = new ArrayList<>();

        try {
            Enumeration<URL> libraryUrls = LibrarySpiLoader.class.getClassLoader().getResources(PATH);
            while (libraryUrls.hasMoreElements()) {
                URL url = libraryUrls.nextElement();
                if (inJar(url)) {
                    if (url.openConnection() instanceof JarURLConnection) {
                        JarURLConnection connection = (JarURLConnection) url.openConnection();
                        JarFile jarFile = connection.getJarFile();
                        Enumeration<JarEntry> entries = jarFile.entries();

                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            String relativePath = entry.getName();
                            if (relativePath.startsWith(PATH) && !entry.isDirectory()) {
                                try (InputStream is = jarFile.getInputStream(entry)) {
                                    String[] split = relativePath.split("/");
                                    LibraryDefinition definition = createDefinition(split[split.length - 1], is);
                                    if (definition != null) {
                                        libraries.add(definition);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    File libraryDirectory = new File(url.getPath());
                    if (libraryDirectory.exists() && libraryDirectory.isDirectory()
                            && libraryDirectory.listFiles() != null) {
                        for (File library : libraryDirectory.listFiles()) {
                            try (FileInputStream fis = new FileInputStream(library.getPath())) {
                                LibraryDefinition definition = createDefinition(library.getName(), fis);
                                if (definition != null) {
                                    libraries.add(definition);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return libraries;
    }

    private static LibraryDefinition createDefinition(String name, InputStream is) {
        Properties properties = new Properties();
        try {
            properties.load(is);
            if (!properties.containsKey("clazz")) {
                return null;
            }
            Class<?> clazz = Class.forName(properties.getProperty("clazz"));
            LibraryDefinition libraryDefinition = new LibraryDefinition();
            libraryDefinition.setName(name);
            libraryDefinition.setClazz(clazz);
            return libraryDefinition;
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean inJar(URL url) {
        return url.getPath().contains(".jar!");
    }

    public static void main(String[] args) {
        initLibrary();
    }
}
