package six.eared.macaque.http.handler;

import io.netty.handler.codec.http.HttpHeaders;
import org.reactivestreams.Publisher;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

public interface RequestHandler<Res> {

    Publisher<Res> process(HttpServerRequest request, HttpServerResponse response);

    default HttpHeaders responseHeader(HttpHeaders headers) {
        return headers;
    }
}
