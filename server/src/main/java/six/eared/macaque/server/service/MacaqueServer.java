package six.eared.macaque.server.service;

import six.eared.macaque.server.config.ServerConfig;


class MacaqueServer implements MacaqueService {

    private final ServerConfig serverConfig;

    public MacaqueServer(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    @Override
    public void start() {
        // TODO start socket lister
    }


    @Override
    public void stop() {

    }
}
