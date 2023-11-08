package six.eared.macaque.agent.compiler.java;


import javax.tools.SimpleJavaFileObject;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class JavaClassFileObject extends SimpleJavaFileObject {

    /**
     *
     */
    protected JavaClassFileObject(File file) {
        super(file.toURI(), Kind.CLASS);
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        return super.openOutputStream();
    }
}
