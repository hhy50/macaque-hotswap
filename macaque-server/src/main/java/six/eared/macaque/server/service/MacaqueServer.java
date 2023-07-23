package six.eared.macaque.server.service;

import six.eared.macaque.common.util.ReflectUtil;
import six.eared.macaque.http.HttpConfig;
import six.eared.macaque.http.MacaqueHttpServer;
import six.eared.macaque.server.config.ServerConfig;
import six.eared.macaque.server.http.ServerHttpInterface;
import six.eared.macaque.server.http.ServerHttpInterfaceHolder;

import java.util.List;
import java.util.stream.Collectors;


class MacaqueServer implements MacaqueService {

    private final ServerConfig serverConfig;

    private MacaqueHttpServer httpServer;

    public MacaqueServer(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    @Override
    public void start() {
        List<Class<? extends ServerHttpInterface>> interfaces = ServerHttpInterfaceHolder.getInterfaces();
        this.httpServer = new MacaqueHttpServer(
                new HttpConfig(this.serverConfig.getServerPort(), "/macaque"),
                interfaces.stream()
                        .distinct()
                        .map(ReflectUtil::newInstance)
                        .collect(Collectors.toList()));
        this.httpServer.start();
    }

    @Override
    public void stop() {
        this.httpServer.stop();
    }
}
