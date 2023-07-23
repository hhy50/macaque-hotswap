package six.eared.macaque.http;

import reactor.core.publisher.Flux;
import reactor.netty.NettyOutbound;
import reactor.netty.http.server.HttpServer;
import six.eared.macaque.http.handler.RequestHandler;
import six.eared.macaque.http.handler.RequestHandlerBuilder;

public class MacaqueHttpServer {

    private final HttpConfig config;
    private final Flux<RequestHandler> requestHandlers;

    public MacaqueHttpServer(HttpConfig config, Flux<RequestHandler> requestHandlers) {
        this.config = config;
        this.requestHandlers = requestHandlers;
    }

    public void start() {
        RequestHandlerBuilder requestHandlerBuilder = new RequestHandlerBuilder(requestHandlers);
        HttpServer.create()
                .port(this.config.getPort())
                .route(requestHandlerBuilder::buildRouters)
                .bindNow(); // Starts the server in a blocking fashion, and waits for it to finish its initialization

    }
}
