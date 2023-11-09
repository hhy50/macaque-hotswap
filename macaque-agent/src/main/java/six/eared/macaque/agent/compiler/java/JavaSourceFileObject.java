package six.eared.macaque.agent.compiler.java;

import com.sun.tools.javac.util.BaseFileManager;
import six.eared.macaque.common.util.FileUtil;

import javax.tools.SimpleJavaFileObject;
import java.io.*;

public class JavaSourceFileObject extends SimpleJavaFileObject {

    private String fileName;

    public JavaSourceFileObject(File file) {
        super(file.toURI(), BaseFileManager.getKind(file.getName()));
        this.fileName = file.getName();
    }

    @Override
    public Kind getKind() {
        return super.getKind();
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return new String(FileUtil.readBytes(super.uri.getPath()));
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
