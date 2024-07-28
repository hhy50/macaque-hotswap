package six.eared.macaque.agent.compiler.java;


import javax.tools.SimpleJavaFileObject;
import java.io.IOException;
import java.net.URI;

public class JavaSourceStringObject extends SimpleJavaFileObject {

    private final String src;

    public JavaSourceStringObject(String fileName, String src) {
        super(URI.create("string:///" +fileName), Kind.SOURCE);
        this.src = src;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return src;
    }
}
