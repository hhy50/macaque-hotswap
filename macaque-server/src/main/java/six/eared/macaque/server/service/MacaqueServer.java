package six.eared.macaque.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import six.eared.macaque.http.HttpConfig;
import six.eared.macaque.http.MacaqueHttpServer;
import six.eared.macaque.server.attach.DefaultAttachFactory;
import six.eared.macaque.server.config.ServerConfig;
import six.eared.macaque.server.http.ServerHttpInterface;
import six.eared.macaque.server.http.interfaces.ClassHotSwapRequestHandler;
import six.eared.macaque.server.http.interfaces.JpsRequestHandler;

import java.util.Arrays;
import java.util.List;


public class MacaqueServer implements MacaqueService {

    private static final Logger log = LoggerFactory.getLogger(MacaqueServer.class);

    private final ServerConfig serverConfig;

    private MacaqueHttpServer httpServer;

    private DefaultAttachFactory defaultAttachFactory;

    public MacaqueServer(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        this.defaultAttachFactory = new DefaultAttachFactory(serverConfig);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void start() {
        String rootPath = "/macaque";
        Integer port = this.serverConfig.getServerPort();
        this.httpServer = new MacaqueHttpServer(
                new HttpConfig(port, rootPath),
                (List) buildInterface());
        this.httpServer.start();

        log.info("MacaqueServer start success, port is {}, rootPath:[{}]", port, rootPath);
    }

    @Override
    public void stop() {
        this.httpServer.stop();
        log.info("MacaqueServer stopped");
    }

    private List<ServerHttpInterface<?>> buildInterface() {
        return Arrays.asList(
                new ClassHotSwapRequestHandler(this.defaultAttachFactory),
                new JpsRequestHandler()
        );
    }
}
