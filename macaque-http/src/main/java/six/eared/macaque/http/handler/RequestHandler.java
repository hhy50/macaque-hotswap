package six.eared.macaque.http.handler;

import org.reactivestreams.Publisher;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

public interface RequestHandler {

    Publisher<Void> process(HttpServerRequest request, HttpServerResponse response);
}
