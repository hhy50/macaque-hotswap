package six.eared.macaque.agent.compiler.java;


import javax.tools.SimpleJavaFileObject;
import java.io.*;

public class JavaClassFileObject extends SimpleJavaFileObject {

    private String fileName;

    /**
     *
     */
    protected JavaClassFileObject(File file) {
        super(file.toURI(), Kind.CLASS);
        this.fileName = file.getName();
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        return new FileOutputStream(super.uri.getPath());
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return new FileInputStream(super.uri.getPath());
    }

    public String getClassName() {
        int index = fileName.lastIndexOf(".");
        if (index != -1) {
            return fileName.substring(0, index);
        }
        return fileName;
    }
}
