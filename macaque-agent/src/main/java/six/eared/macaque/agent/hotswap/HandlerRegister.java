package six.eared.macaque.agent.hotswap;

import six.eared.macaque.agent.annotation.HotSwapFileType;
import six.eared.macaque.agent.hotswap.handler.HotSwapHandler;
import six.eared.macaque.common.type.FileType;
import six.eared.macaque.common.util.ClassUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HandlerRegister {

    public static final Map<FileType, HotSwapHandler> HANDLERS = new HashMap<>();

    private static final String HANDLER_CLASS_PATH = "six.eared.macaque.agent.hotswap.handler";

//    static {
//        registerHandler(new ClassHotSwapHandler());
//        registerHandler(new XmlFileHandler());
//    }
//
//    public static void registerHandler(HotSwapHandler handler) {
//        Class<? extends HotSwapHandler> clazz = handler.getClass();
//        HotSwapFileType hotSwapHandler = clazz.getDeclaredAnnotation(HotSwapFileType.class);
//        HANDLERS.putIfAbsent(hotSwapHandler.fileType(), handler);
//    }

    static {
        try {
            init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized static void init() throws Exception {
        if (!HANDLERS.isEmpty()) {
            return;
        }
        List<Class> classes = ClassUtil.scanClass(HANDLER_CLASS_PATH);
        for (Class clazz : classes) {
            if (!HotSwapHandler.class.isAssignableFrom(clazz)) {
                continue;
            }
            Class<? extends HotSwapHandler> handlerClazz = (Class<? extends HotSwapHandler>) clazz;
            HotSwapFileType annotation = handlerClazz.getDeclaredAnnotation(HotSwapFileType.class);
            if (annotation == null) {
                continue;
            }
            HANDLERS.putIfAbsent(annotation.fileType(), handlerClazz.getConstructor().newInstance());
        }

    }

    public static HotSwapHandler getHandler(String fileType) throws Exception {
        return HANDLERS.get(FileType.ofType(fileType));
    }
}
