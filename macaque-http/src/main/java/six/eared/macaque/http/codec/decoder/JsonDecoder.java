package six.eared.macaque.http.codec.decoder;

import com.alibaba.fastjson.JSONObject;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import six.eared.macaque.common.util.StringUtil;


public class JsonDecoder<Req> extends BaseDecoder<Req> {

    @Override
    public Mono<Req> decode(HttpServerRequest request, Class<Req> reqType) {
        if (request.method() == HttpMethod.POST
                && isJsonRequest(request)) {
            return readFromRequestBody(request)
                    .defaultIfEmpty(StringUtil.EMPTY_STR)
                    .map((item) -> JSONObject.parseObject(item, reqType));
        }
        return Mono.empty();
    }

    private boolean isJsonRequest(HttpServerRequest request) {
        HttpHeaders headers = request.requestHeaders();

        String contentType = headers.get(HttpHeaderNames.CONTENT_TYPE);
        return StringUtil.isNotEmpty(contentType) && contentType.contains("application/json");
    }

    private Mono<String> readFromRequestBody(HttpServerRequest request) {
        return Mono.from(request.receive().asString());
    }
}
