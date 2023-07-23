package six.eared.macaque.http.codec.impl;

import cn.hutool.core.bean.BeanUtil;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;

import java.util.Map;

public class FormDecoder<Req> extends BaseDecoder<Req> {

    public FormDecoder(Class<Req> reqType) {
        super(reqType);
    }

    @Override
    public Mono<Req> decode(HttpServerRequest request) {
        if (request.isFormUrlencoded()) {
            Map<String, String> params = request.params();
            return Mono.just(BeanUtil.toBean(params, reqType));
        }
        return Mono.empty();
    }
}
