package six.eared.macaque.http.codec;

import reactor.core.publisher.Flux;
import reactor.netty.http.server.HttpServerRequest;

public interface Decoder<T> {

    public Flux<T> decode(HttpServerRequest request);
}
