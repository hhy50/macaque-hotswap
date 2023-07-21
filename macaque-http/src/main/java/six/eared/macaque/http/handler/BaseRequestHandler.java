package six.eared.macaque.http.handler;


import io.netty.handler.codec.http.HttpHeaders;
import reactor.core.publisher.Flux;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

public abstract class BaseRequestHandler<Req, Res> implements RequestHandler<Res> {

    @SuppressWarnings("unchecked")
    @Override
    public Flux<Res> process(HttpServerRequest request, HttpServerResponse response) {


        return null;
    }

    private boolean isJsonRequest(HttpServerRequest request) {

        return false;
    }

    @Override
    public HttpHeaders responseHeader(HttpHeaders headers) {


        return headers;
    }

    public abstract Object process0(Req req);
}
