package six.eared.macaque.server.service;

import six.eared.macaque.server.config.ServerConfig;

public class DefaultServiceFactoryImpl implements ServiceFactory {

    public DefaultServiceFactoryImpl() {

    }

    @Override
    public MacaqueService newService(boolean serverMode, ServerConfig serverConfig) {
        return serverMode ? new MacaqueServer(serverConfig)
                : new MacaqueConsole(serverConfig);
    }
}
