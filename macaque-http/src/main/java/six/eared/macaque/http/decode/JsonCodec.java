package six.eared.macaque.http.decode;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import reactor.core.publisher.Flux;
import reactor.netty.http.server.HttpServerRequest;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public abstract class JsonCodec<Req> implements Codec<Req, String> {

    @Override
    public Req decode(HttpServerRequest request) {
        Class<Req> reqType = getReqType();
        StringBuilder sb = new StringBuilder();

        Map<String, Object> reqObj = new HashMap<>();
        readStrFromRequestBody(request)
                .doOnComplete(() -> {
                    String json = sb.toString();
                    JSONObject jsonObject = JSONObject.parseObject(json);
                    reqObj.putAll(jsonObject);
                })
                .subscribe(str -> {
                    sb.append(str);
                });
        return BeanUtil.toBean(reqObj, reqType);
    }

    @Override
    public String encode(Object obj) {
        return JSONObject.toJSONString(obj);
    }

    @SuppressWarnings("unchecked")
    public Class<Req> getReqType() {
        ParameterizedType superGenericSuperclass = (ParameterizedType) this.getClass().getGenericSuperclass();
        Type[] types = superGenericSuperclass.getActualTypeArguments();
        try {
            return (Class<Req>) Class.forName(types[0].getTypeName());
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private Flux<String> readStrFromRequestBody(HttpServerRequest request) {
        return request.receive().asString();
    }
}
