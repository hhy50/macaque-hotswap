package six.eared.macaque.http.codec.impl;

import reactor.core.publisher.Flux;
import reactor.netty.http.server.HttpServerRequest;

public class FormDecoder<Req> extends BaseDecoder<Req> {

    public FormDecoder(Class<Req> reqType) {
        super(reqType);
    }

    @Override
    public Flux<Req> decode(HttpServerRequest request) {
        return Flux.empty();
    }
}
