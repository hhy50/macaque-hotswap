package six.eared.macaque.server.service;

import six.eared.macaque.server.config.ServerConfig;

public interface ServiceFactory {

    /**
     *
     * @param serverMode
     * @param serverConfig
     * @return
     */
    public MacaqueService newService(boolean serverMode, ServerConfig serverConfig);
}
