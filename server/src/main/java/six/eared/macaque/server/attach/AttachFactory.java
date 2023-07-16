package six.eared.macaque.server.attach;

import six.eared.macaque.server.config.ServerConfig;

public interface AttachFactory {

    /**
     *
     * @param serverConfig
     * @return
     */
    public Attach createRuntimeAttach(ServerConfig serverConfig);
}
