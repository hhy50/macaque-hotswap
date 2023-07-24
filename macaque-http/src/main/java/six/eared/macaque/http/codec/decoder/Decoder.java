package six.eared.macaque.http.codec.decoder;

import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;

public interface Decoder<T> {

    public Mono<T> decode(HttpServerRequest request, Class<T> type);
}
