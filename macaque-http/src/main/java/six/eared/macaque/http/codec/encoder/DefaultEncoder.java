package six.eared.macaque.http.codec.encoder;

import com.alibaba.fastjson.JSONObject;
import reactor.core.publisher.Mono;

public class DefaultEncoder extends BaseEncoder {

    @Override
    public Mono<String> encode(Mono<Object> obj) {
        return obj.map(JSONObject::toJSONString);
    }
}
