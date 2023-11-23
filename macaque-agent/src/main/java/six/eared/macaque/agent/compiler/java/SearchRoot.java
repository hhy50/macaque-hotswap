package six.eared.macaque.agent.compiler.java;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.List;
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

    default void close() {

    }
}
