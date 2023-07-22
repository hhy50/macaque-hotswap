package six.eared.macaque.http.decode;

import cn.hutool.core.util.ReflectUtil;
import reactor.netty.http.server.HttpServerRequest;


public abstract class UrlVariableCodec<Req> extends BaseCodec<Req, Object> {
    @Override
    public Req decode(HttpServerRequest request) {
        String uri = request.uri();

        if (uri.contains("?")) {
            String urlParams = uri.split("\\?")[1];

            Req req = newReqObj();
            for (String param : urlParams.split("&")) {
                String[] kv = param.split("=");
                try {
                    ReflectUtil.setFieldValue(req, kv[0], kv[1]);
                } catch (Exception e) {

                }
            }
            return req;
        }
        return null;
    }

    @Override
    public Object encode(Object obj) {
        return null;
    }
}
