package six.eared.macaque.http.handler;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ReflectUtil;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import six.eared.macaque.http.decode.*;
import six.eared.macaque.http.response.ErrorResponse;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;


public abstract class BaseRequestHandler<Req> implements RequestHandler<Object> {

    private static final String CONTENT_TYPE = "Content-Type";

    private final JsonCodec<Req> jsonCodec = new JsonCodec<Req>() {};

    private final String ERROR = jsonCodec.encode(new ErrorResponse("error"));

    private static final Logger log = LoggerFactory.getLogger(BaseRequestHandler.class);

    @SuppressWarnings("unchecked")
    @Override
    public final Flux<Object> process(HttpServerRequest request, HttpServerResponse response) {
        try {
            return Flux.just(request)
                    .mapNotNull(req -> {
                        AtomicReference<Req> reqReference = new AtomicReference<>();
                        buildDecoder(request)
                                .map(encoder -> encoder.decode(request))
                                .filter(Objects::nonNull)
                                .collectList()
                                .map((reqs) -> mergeMultiEntry(reqs, getReqType()))
                                .subscribe(item -> {
                                    reqReference.set(item);
                                });
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        return reqReference.get();
                    })
                    .map(this::process0)
                    .mapNotNull(res -> {
                        setJsonResponse(response);
                        return jsonCodec.encode(res);
                    });
        } catch (Exception e) {
            log.error("http process error", e);
        }
        return error();
    }

    private void setJsonResponse(HttpServerResponse response) {
        response.header(CONTENT_TYPE, "application/json;charset=UTF-8");
    }

    private Flux<Object> error() {
        return Flux.just(ERROR);
    }

    private Flux<Decoder<Req>> buildDecoder(HttpServerRequest request) {
        Flux<Decoder<Req>> codecChain = null;

        boolean get = isGet(request);
        if (get) {
            codecChain = Flux.just(new UrlVariableCodec<Req>() {});
        } else {
            if (isJsonRequest(request)) {
                codecChain = Flux.just(jsonCodec, new UrlVariableCodec<Req>() {});
            } else {
                codecChain = Flux.just(new FormCodec<>(),
                        new FileCodec<>(),
                        new UrlVariableCodec<Req>() {});
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
