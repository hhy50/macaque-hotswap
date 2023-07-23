package six.eared.macaque.http.handler;


import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import six.eared.macaque.common.util.CollectionUtil;
import six.eared.macaque.common.util.ReflectUtil;
import six.eared.macaque.http.codec.Decoder;
import six.eared.macaque.http.codec.impl.FormDecoder;
import six.eared.macaque.http.codec.impl.JsonCodec;
import six.eared.macaque.http.codec.impl.UrlVariableDecoder;
import six.eared.macaque.http.response.ErrorResponse;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;


public abstract class BaseRequestHandler<Req> implements RequestHandler {

    protected static final String CONTENT_TYPE = "Content-Type";

    protected final Class<Req> reqType = getReqType();

    protected final JsonCodec<Req> jsonCodec = new JsonCodec<>(reqType);

    protected final ErrorResponse ERROR = new ErrorResponse("error");

    private static final Logger log = LoggerFactory.getLogger(BaseRequestHandler.class);

    @SuppressWarnings("unchecked")
    @Override
    public final Publisher<Void> process(HttpServerRequest request, HttpServerResponse response) {
        setJsonResponse(response);
        return response.sendString(Mono.just(request)
                .map(req -> {
                    return buildDecoder(request)
                            .map(encoder -> {
                                return encoder.decode(request);
                            })
                            .flatMap(item -> item)
                            .collectList()
                            .map((reqs) -> {
                                return mergeMultiEntry(reqs, reqType);
                            });
                })
                .flatMap(item -> item)
                .map((req) -> {
                    try {
                        Object res = this.process0(req);
                        return res == null ? ERROR : res;
                    } catch (Exception e) {
                        log.error("http process error", e);
                    }
                    return ERROR;
                })
                .transform(res -> {
                    return jsonCodec.encode(res);
                }));
    }

    private void setJsonResponse(HttpServerResponse response) {
        response.responseHeaders().add(CONTENT_TYPE, "application/json;charset=UTF-8");
    }


    private Flux<Decoder<Req>> buildDecoder(HttpServerRequest request) {
        Flux<Decoder<Req>> codecChain = null;
        boolean get = isGet(request);
        if (get) {
            codecChain = Flux.just(new UrlVariableDecoder<>(reqType));
        } else {
            if (isJsonRequest(request)) {
                codecChain = Flux.just(jsonCodec, new UrlVariableDecoder<>(reqType));
            } else {
                codecChain = Flux.just(new FormDecoder<>(reqType),
                        new UrlVariableDecoder<>(reqType));
            }
        }
        return codecChain;
    }

    private boolean isGet(HttpServerRequest request) {
        return request.method() == HttpMethod.GET;
    }

    private static <T> T mergeMultiEntry(List<T> objs, Class<T> type) {
        if (CollectionUtil.isNotEmpty(objs)) {
            if (objs.size() == 1) {
                return objs.get(0);
            }

            Field[] fields = ReflectUtil.getFields(type);
            try {
                T obj = objs.get(0);
                for (Field field : fields) {
                    field.setAccessible(true);
                    if (field.get(obj) == null) {
                        for (int i = 1; i < objs.size(); i++) {
                            Object fv = field.get(objs.get(i));
                            if (fv == null) continue;
                            field.set(obj, fv);
                        }
                    }
                }
                return obj;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private boolean isJsonRequest(HttpServerRequest request) {
        HttpHeaders headers = request.requestHeaders();

        String contentType = headers.get(CONTENT_TYPE);
        return contentType.contains("application/json");
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
