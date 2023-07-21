package six.eared.macaque.http.decode;

import reactor.netty.http.server.HttpServerRequest;

public interface Decoder<T> {

    public T decode(HttpServerRequest request);
}
