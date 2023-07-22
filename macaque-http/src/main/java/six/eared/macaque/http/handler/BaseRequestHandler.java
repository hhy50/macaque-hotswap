package six.eared.macaque.http.handler;


import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import six.eared.macaque.http.decode.*;

import java.util.List;


public abstract class BaseRequestHandler<Req, Res> implements RequestHandler<Res> {

    private static final Logger log = LoggerFactory.getLogger(BaseRequestHandler.class);

    @SuppressWarnings("unchecked")
    @Override
    public final Flux<Res> process(HttpServerRequest request, HttpServerResponse response) {
        Flux<Decoder<Req>> decoderChain = buildDecoder(request, response);
        Flux<Encoder<Res>> encoderChain = buildEncoder(request, response);
        try {
            return decoderChain
                    .map(decoder -> decoder.decode(request))
                    .collectList()
                    .map((reqs) -> mergeMultiEntry(reqs))
                    .map((req) -> process0(req))
                    .as(res -> {
                        return encoderChain
                                .map(encoder -> encoder.encode(res))
                                .collectList()
                                .map(list -> mergeMultiEntry(list));
                    }).flux();
        } catch (Exception e) {
            log.error("http process error", e);
        }
        return error();
    }

    private Flux<Res> error() {
        return null;
    }

    private Flux<Decoder<Req>> buildDecoder(HttpServerRequest request, HttpServerResponse response) {
        Flux<Decoder<Req>> codecChain = null;

        boolean get = isGet(request);
        if (get) {
            codecChain = Flux.just(new UrlVariableCodec<>());
        } else {
            if (isJsonRequest(request)) {
                codecChain = Flux.just(new JsonCodec<>(), new UrlVariableCodec<>());
            } else {
                codecChain = Flux.just(new FormCodec<>(),
                        new FileCodec<>(),
                        new UrlVariableCodec<>());
            }
        }
        return codecChain;
    }

    private Flux<Encoder<Res>> buildEncoder(HttpServerRequest request, HttpServerResponse response) {
        return Flux.empty();
    }

    private boolean isGet(HttpServerRequest request) {
        return request.method() == HttpMethod.GET;
    }

    private static <T> T mergeMultiEntry(List<T> reqs) {


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
