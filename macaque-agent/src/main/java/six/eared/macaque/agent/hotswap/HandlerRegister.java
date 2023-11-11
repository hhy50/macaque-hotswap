package six.eared.macaque.agent.hotswap;

import six.eared.macaque.agent.annotation.HotSwapFileType;
import six.eared.macaque.agent.hotswap.handler.ClassHotSwapHandler;
import six.eared.macaque.agent.hotswap.handler.HotSwapHandler;
import six.eared.macaque.agent.hotswap.handler.JavaFileHotswapHandler;
import six.eared.macaque.agent.hotswap.handler.XmlFileHandler;
import six.eared.macaque.common.type.FileType;

import java.util.HashMap;
import java.util.Map;

public class HandlerRegister {

    public static final Map<FileType, HotSwapHandler> HANDLERS = new HashMap<>();

    static {
        ClassHotSwapHandler classHotSwapHandler = new ClassHotSwapHandler();

        registerHandler(classHotSwapHandler);
        registerHandler(new XmlFileHandler());
        registerHandler(new JavaFileHotswapHandler(classHotSwapHandler));
    }

    public static void registerHandler(HotSwapHandler handler) {
        Class<? extends HotSwapHandler> clazz = handler.getClass();
        HotSwapFileType hotSwapHandler = clazz.getDeclaredAnnotation(HotSwapFileType.class);
        HANDLERS.putIfAbsent(hotSwapHandler.fileType(), handler);
    }

    public static HotSwapHandler getHandler(String fileType) {
        return HANDLERS.get(FileType.ofType(fileType));
    }
}
