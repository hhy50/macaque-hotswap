package six.eared.macaque.http.codec.impl;

import cn.hutool.core.util.ReflectUtil;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;


public class UrlVariableDecoder<Req> extends BaseDecoder<Req> {

    public UrlVariableDecoder(Class<Req> reqType) {
        super(reqType);
    }

    @Override
    public Mono<Req> decode(HttpServerRequest request) {
        String uri = request.uri();

        if (uri.contains("?")) {
            Req req = newReqObject();
            String urlParams = uri.split("\\?")[1];
            for (String param : urlParams.split("&")) {
                String[] kv = param.split("=");
                try {
                    ReflectUtil.setFieldValue(req, kv[0], kv[1]);
                } catch (Exception e) {

                }
            }
            return Mono.just(req);
        }
        return Mono.empty();
    }
}
