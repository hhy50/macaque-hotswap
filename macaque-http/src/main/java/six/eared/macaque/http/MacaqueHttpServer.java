package six.eared.macaque.http;

import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import six.eared.macaque.http.handler.RequestHandler;
import six.eared.macaque.http.handler.RequestHandlerBuilder;

import java.util.List;

public class MacaqueHttpServer {

    private final HttpConfig config;
    private final List<RequestHandler> requestHandlers;

    private HttpServer server;

    private DisposableServer disposableServer;

    public MacaqueHttpServer(HttpConfig config, List<RequestHandler> requestHandlers) {
        this.config = config;
        this.requestHandlers = requestHandlers;
    }

    public void start() {
        RequestHandlerBuilder requestHandlerBuilder = new RequestHandlerBuilder(this.config.getRootPath(), requestHandlers);
        this.server = HttpServer.create()
                .port(this.config.getPort())
                .route(requestHandlerBuilder::buildRouters);
        this.disposableServer = this.server.bindNow();
    }

    public void stop() {
        this.disposableServer.dispose();
    }
}
