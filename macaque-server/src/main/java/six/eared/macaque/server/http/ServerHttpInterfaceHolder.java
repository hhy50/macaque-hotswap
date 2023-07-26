package six.eared.macaque.server.http;


import six.eared.macaque.server.http.interfaces.ClassHotSwapRequestHandler;
import six.eared.macaque.server.http.interfaces.JpsRequestHandler;

import java.util.ArrayList;
import java.util.List;

public class ServerHttpInterfaceHolder {
    private static final List<Class<? extends ServerHttpInterface>>
            INTERFACES = new ArrayList<>();

    static {
        addInterface((Class) ClassHotSwapRequestHandler.class);
        addInterface((Class) JpsRequestHandler.class);
    }

    public static void addInterface(Class<ServerHttpInterface> httpInterface) {
        INTERFACES.add(httpInterface);
    }

    public static List<Class<? extends ServerHttpInterface>> getInterfaces() {
        return INTERFACES;
    }
}
