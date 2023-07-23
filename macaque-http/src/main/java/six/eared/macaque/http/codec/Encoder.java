package six.eared.macaque.http.codec;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface Encoder<T> {

    public Mono<T> encode(Mono<Object> obj);
}
