package six.eared.macaque.agent.hotswap.handler;

import six.eared.macaque.agent.annotation.HotSwapFileType;
import six.eared.macaque.agent.compiler.java.JavaSourceCompiler;
import six.eared.macaque.agent.exceptions.HotswapException;
import six.eared.macaque.common.type.FileType;
import six.eared.macaque.common.util.CollectionUtil;
import six.eared.macaque.common.util.StringUtil;
import six.eared.macaque.mbean.rmi.HotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@HotSwapFileType(fileType = FileType.Java)
public class JavaFileHotswapHandler implements HotSwapHandler {

    private ClassHotSwapHandler classHotSwapHandler;

    public JavaFileHotswapHandler(ClassHotSwapHandler classHotSwapHandler) {
        this.classHotSwapHandler = classHotSwapHandler;
    }

    @Override
    public RmiResult handlerRequest(HotSwapRmiData request) {
        if (!JavaSourceCompiler.getInstance().isPrepared()) {
            throw new HotswapException("current JDK env not support memory compile");
        }

        if (StringUtil.isEmpty(request.getFileName())) {
            throw new HotswapException("file type is 'java', fileName must not be null");
        }

        try {
            Map<String, byte[]> sources = new HashMap<>();
            sources.put(request.getFileName(), request.getFileData());

            List<byte[]> compiled = JavaSourceCompiler.getInstance().compile(sources);
            if (CollectionUtil.isNotEmpty(compiled)) {
                request.setFileData(mergeClassData(compiled));
                request.setFileType(FileType.Class.getType());
                return classHotSwapHandler.handlerRequest(request);
            }
            throw new HotswapException("compile error,  no output class file");
        } catch (Exception e) {
            if (e instanceof HotswapException) {
                throw e;
            }
            throw new HotswapException(e);
        }
    }

    public static byte[] mergeClassData(List<byte[]> byteList) {
        return byteList.stream().reduce((b1, b2) -> {
            int len = b1.length + b2.length;
            byte[] bytes = new byte[len];

            System.arraycopy(b1, 0, bytes, 0, b1.length);
            System.arraycopy(b2, 0, bytes, b1.length, b2.length);
            return bytes;
        }).get();
    }
}
