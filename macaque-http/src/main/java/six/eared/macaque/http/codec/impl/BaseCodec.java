package six.eared.macaque.http.codec.impl;


import six.eared.macaque.http.codec.Decoder;
import six.eared.macaque.http.codec.Encoder;

public abstract class BaseCodec<Req, Res> implements Encoder<Res>, Decoder<Req> {

    protected final Class<Req> reqType;
    protected final Class<Res> resType;

    protected BaseCodec(Class<Req> reqType, Class<Res> resType) {
        this.reqType = reqType;
        this.resType = resType;
    }
}
