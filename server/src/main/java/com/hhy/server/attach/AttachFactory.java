package com.hhy.server.attach;

import com.hhy.server.config.ServerConfig;

public interface AttachFactory {

    /**
     *
     * @param serverConfig
     * @return
     */
    public Attach createRuntimeAttach(ServerConfig serverConfig);
}
