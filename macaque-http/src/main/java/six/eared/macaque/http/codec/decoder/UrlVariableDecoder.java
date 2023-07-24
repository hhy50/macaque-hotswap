package six.eared.macaque.http.codec.decoder;

import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import six.eared.macaque.common.util.ReflectUtil;


public class UrlVariableDecoder<Req> extends BaseDecoder<Req> {

    @Override
    public Mono<Req> decode(HttpServerRequest request, Class<Req> reqType) {
        String uri = request.uri();
        if (uri.contains("?")) {
            Req req = newReqObject(reqType);
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
