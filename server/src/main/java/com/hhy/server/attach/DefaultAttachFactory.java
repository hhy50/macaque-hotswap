package com.hhy.server.attach;

import com.hhy.server.config.ServerConfig;

public class DefaultAttachFactory implements AttachFactory {
    private static final DefaultAttachFactory INSTANCE = new DefaultAttachFactory();

    private DefaultAttachFactory() {

    }

    public static DefaultAttachFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public Attach createRuntimeAttach(ServerConfig serverConfig) {
        return new RuntimeAttach(serverConfig);
    }
}
