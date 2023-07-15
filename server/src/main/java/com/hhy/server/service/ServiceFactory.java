package com.hhy.server.service;

import com.hhy.server.config.ServerConfig;

public interface ServiceFactory {

    /**
     *
     * @param serverMode
     * @param serverConfig
     * @return
     */
    public MacaqueService newService(boolean serverMode, ServerConfig serverConfig);
}
