package com.hhy.server.attach;

import com.hhy.server.config.ServerConfig;

public interface AttachFactory {

    public Attach createRuntimeAttach(ServerConfig serverConfig);
}
