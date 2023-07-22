package six.eared.macaque.http.decode;

import reactor.netty.http.server.HttpServerRequest;

public class UrlVariableCodec<Req> extends BaseCodec<Req, Object> {
    @Override
    public Req decode(HttpServerRequest request) {
        return null;
    }

    @Override
    public Object encode(Object obj) {
        return null;
    }
}
