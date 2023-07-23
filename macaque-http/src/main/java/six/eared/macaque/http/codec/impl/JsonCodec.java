package six.eared.macaque.http.codec.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import six.eared.macaque.common.util.StringUtil;

public class JsonCodec<Req> extends BaseCodec<Req, String> {

    public JsonCodec(Class<Req> reqType) {
        super(reqType, String.class);
    }

    @Override
    public Mono<Req> decode(HttpServerRequest request) {
        String body = readFromRequestBody(request);
        if (StringUtil.isNotEmpty(body)) {
            JSONObject jsonObject = JSONObject.parseObject(body);
            return Mono.just(BeanUtil.toBean(jsonObject, reqType));
        }
        return Mono.empty();
    }

    @Override
    public Mono<String> encode(Mono<Object> obj) {
        return Mono.just(JSONObject.toJSONString(obj));
    }

    private String readFromRequestBody(HttpServerRequest request) {
        return request.receive().asString()
                .toIterable().iterator().next();
    }
}
