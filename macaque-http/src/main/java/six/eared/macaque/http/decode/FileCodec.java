package six.eared.macaque.http.decode;

import reactor.netty.http.server.HttpServerRequest;

import java.io.File;

public class FileCodec<Req> extends BaseCodec<Req, byte[]>{

    @Override
    public Req decode(HttpServerRequest request) {
        return null;
    }

    @Override
    public byte[] encode(Object obj) {
        if (obj instanceof byte[]) {

        }
        if (obj instanceof File) {

        }
        return new byte[0];
    }
}
