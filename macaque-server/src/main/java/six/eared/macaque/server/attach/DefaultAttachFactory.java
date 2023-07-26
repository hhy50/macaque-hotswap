package six.eared.macaque.server.attach;

import six.eared.macaque.server.config.ServerConfig;

import java.util.HashMap;
import java.util.Map;

public class DefaultAttachFactory implements AttachFactory {

    private static final Map<String, RuntimeAttach> HISTORY = new HashMap<>();

    private ServerConfig serverConfig;

    public DefaultAttachFactory(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    @Override
    public synchronized Attach createRuntimeAttach(String pid) {
        RuntimeAttach attach = HISTORY.get(pid);
        if (attach != null) {
            return attach;
        }
        RuntimeAttach runtimeAttach = new RuntimeAttach(pid, this.serverConfig);
        HISTORY.put(pid, runtimeAttach);
        return runtimeAttach;
    }
}
