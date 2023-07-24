package six.eared.macaque.http.codec.encoder;

import reactor.core.publisher.Mono;

public interface Encoder {

    public Mono<String> encode(Mono<Object> obj);
}
