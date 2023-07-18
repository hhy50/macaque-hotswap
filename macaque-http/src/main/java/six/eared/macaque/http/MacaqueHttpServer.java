package six.eared.macaque.http;

import reactor.netty.http.server.HttpServer;

public class MacaqueHttpServer {

    private final HttpConfig config;

    public MacaqueHttpServer(HttpConfig config) {
        this.config = config;
    }

    public void start() {
        HttpServer.create()
                .port(this.config.getPort())
                .route(routes ->
                        // The server will respond only on POST requests
                        // where the path starts with /test and then there is path parameter
                        routes.get("/test/{param}", (request, response) ->
                                response.sendString(request.receive()
                                        .asString()
                                        .map(s -> s + ' ' + request.param("param") + '!')
                                        .log("http-server"))))
                .bindNow(); // Starts the server in a blocking fashion, and waits for it to finish its initialization

    }
}
