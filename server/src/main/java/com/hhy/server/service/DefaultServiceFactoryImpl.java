package com.hhy.server.service;

import com.hhy.server.config.ServerConfig;

public class DefaultServiceFactoryImpl implements ServiceFactory {

    private static final DefaultServiceFactoryImpl INSTANCE = new DefaultServiceFactoryImpl();

    private DefaultServiceFactoryImpl() {

    }

    public static DefaultServiceFactoryImpl getINSTANCE() {
        return INSTANCE;
    }

    @Override
    public MacaqueService newService(boolean serverMode, ServerConfig serverConfig) {
        return serverMode ? new MacaqueServer(serverConfig)
                : new MacaqueConsole(serverConfig);
    }
}
