package six.eared.macaque.agent.compiler.java;

import six.eared.macaque.common.util.FileUtil;

import javax.tools.SimpleJavaFileObject;
import java.io.File;
import java.io.IOException;

public class JavaSourceFileObject extends SimpleJavaFileObject {

    public JavaSourceFileObject(File file) {
        super(file.toURI(), Kind.SOURCE);
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return new String(FileUtil.readBytes(super.uri.getPath()));
    }
}
