package six.eared.macaque.http.handler;


import org.reactivestreams.Publisher;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import six.eared.macaque.http.codec.combiner.DecoderCombiner;
import six.eared.macaque.http.codec.decoder.Decoder;
import six.eared.macaque.http.codec.decoder.FormDecoder;
import six.eared.macaque.http.codec.decoder.JsonDecoder;
import six.eared.macaque.http.codec.decoder.UrlVariableDecoder;
import six.eared.macaque.http.codec.encoder.DefaultEncoder;
import six.eared.macaque.http.codec.encoder.Encoder;
import six.eared.macaque.http.response.ErrorResponse;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;

public abstract class BaseRequestHandler<Req> implements RequestHandler {

    protected static final ErrorResponse ERROR = new ErrorResponse("error");

    @SuppressWarnings("unchecked")
    @Override
    public final Publisher<Void> process(HttpServerRequest request, HttpServerResponse response) {
        setJsonResponse(response);
        Decoder<Req> decoder = buildDecoder();
        Encoder encoder = buildEncoder();

        return response.sendString(
                decoder.decode(request, getReqType())
                        .map((req) -> {
                            try {
                                Object res = this.process0(req);
                                return res == null ? ERROR : res;
                            } catch (Exception e) {
                                return ERROR;
                            }
                        })
                        .transform(res -> {
                            return encoder.encode(res);
                        })
        );
    }

    private void setJsonResponse(HttpServerResponse response) {
        response.responseHeaders().add(CONTENT_TYPE, "application/json;charset=UTF-8");
    }

    private Encoder buildEncoder() {
        return new DefaultEncoder();
    }

    private Decoder<Req> buildDecoder() {
        return DecoderCombiner.<Req>builder()
                .next(new JsonDecoder<>())
                .next(new UrlVariableDecoder<>())
                .next(new FormDecoder<>())
                .build();
    }


    @SuppressWarnings("unchecked")
    protected Class<Req> getReqType() {
        ParameterizedType superGenericSuperclass = (ParameterizedType) this.getClass().getGenericSuperclass();
        Type[] types = superGenericSuperclass.getActualTypeArguments();
        try {
            return (Class<Req>) Class.forName(types[0].getTypeName());
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public abstract Object process0(Req req);
}
