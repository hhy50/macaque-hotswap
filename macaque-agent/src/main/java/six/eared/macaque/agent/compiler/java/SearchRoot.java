package six.eared.macaque.agent.compiler.java;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SearchRoot {

    /**
     *
     * @param packageName
     * @param kinds
     * @return
     * @throws IOException
     */
    public List<JavaFileObject> search(String packageName, Set<JavaFileObject.Kind> kinds) throws IOException;

    /**
     *
     * @return
     */
    default Map<String, ClassLoader> searchAnnotationProcessors(List<URL> processorClasspath) {
        return Collections.emptyMap();
    }

    default void close() {

    }
}
