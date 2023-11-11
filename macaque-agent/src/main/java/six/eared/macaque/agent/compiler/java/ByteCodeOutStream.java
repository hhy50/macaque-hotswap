package six.eared.macaque.agent.compiler.java;


import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;


public class ByteCodeOutStream extends SimpleJavaFileObject {
    private static final char PKG_SEPARATOR = '.';
    private static final char DIR_SEPARATOR = '/';

    private final String className;
    private ByteArrayOutputStream byteArrayOutputStream;

    protected ByteCodeOutStream(String className) {
        super(URI.create("byte:///" + className.replace(PKG_SEPARATOR, DIR_SEPARATOR)
                + Kind.CLASS.extension), Kind.CLASS);

        this.className = className;
    }


    @Override
    public OutputStream openOutputStream() {
        if (byteArrayOutputStream == null) {
            byteArrayOutputStream = new ByteArrayOutputStream();
        }
        return byteArrayOutputStream;
    }

    public byte[] getByteCode() {
        return byteArrayOutputStream.toByteArray();
    }

    public String getClassName() {
        return className;
    }
}
