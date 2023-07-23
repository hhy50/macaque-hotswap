package six.eared.macaque.http.codec.impl;

import com.alibaba.fastjson.JSONObject;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import six.eared.macaque.common.util.StringUtil;

public class JsonCodec<Req> extends BaseCodec<Req, String> {

    public JsonCodec(Class<Req> reqType) {
        super(reqType, String.class);
    }

    @Override
    public Flux<Req> decode(HttpServerRequest request) {
        return readFromRequestBody(request)
                .defaultIfEmpty(StringUtil.EMPTY_STR)
                .map((item) -> JSONObject.parseObject(item, reqType));
    }

    @Override
    public Mono<String> encode(Mono<Object> obj) {
        return obj.map(JSONObject::toJSONString);
    }

    private Flux<String> readFromRequestBody(HttpServerRequest request) {
        return request.receive().asString();
    }
}
