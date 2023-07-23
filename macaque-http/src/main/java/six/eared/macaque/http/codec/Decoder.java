package six.eared.macaque.http.codec;

import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;

public interface Decoder<T> {

    public Mono<T> decode(HttpServerRequest request);
}
