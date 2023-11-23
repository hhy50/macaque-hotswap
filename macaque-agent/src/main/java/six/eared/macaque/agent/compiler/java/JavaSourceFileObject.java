package six.eared.macaque.agent.compiler.java;


import six.eared.macaque.common.util.FileUtil;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;

public class JavaSourceFileObject implements JavaFileObject {
    private final URI uri;
    private final String className;
    private final Kind kind;

    public JavaSourceFileObject(File file) {
        this.uri = file.toURI();
        this.kind = DynamicJavaFileManager.getKind(file.getName());
        this.className = FileUtil.getFileName(file.getName());
    }

    public JavaSourceFileObject(URI uri, String className, Kind kind) {
        this.uri = uri;
        this.kind = kind;
        this.className = className;
    }

    public URI toUri() {
        return this.uri;
    }

    public InputStream openInputStream() throws IOException {
        return this.uri.toURL().openStream();
    }

    public OutputStream openOutputStream() {
        throw new UnsupportedOperationException();
    }

    public String getName() {
        return this.className;
    }

    public Reader openReader(boolean ignoreEncodingErrors) {
        throw new UnsupportedOperationException();
    }

    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return new String(FileUtil.readBytes(uri.getPath()));
    }

    public Writer openWriter() throws IOException {
        throw new UnsupportedOperationException();
    }

    public long getLastModified() {
        return 0;
    }

    public boolean delete() {
        throw new UnsupportedOperationException();
    }

    public Kind getKind() {
        return this.kind;
    }

    public boolean isNameCompatible(String simpleName, Kind kind) {
        return kind.equals(getKind())
                && this.className.endsWith(simpleName);
    }

    public NestingKind getNestingKind() {
        throw new UnsupportedOperationException();
    }

    public Modifier getAccessLevel() {
        throw new UnsupportedOperationException();
    }


    public String toString() {
        return this.getClass().getName() + "[" + this.toUri() + "]";
    }
}
