package com.hhy.server.server;

import com.hhy.server.config.ServerConfig;

public interface ServiceFactory {

    public MacaqueService newService(boolean serverMode, ServerConfig serverConfig);
}
