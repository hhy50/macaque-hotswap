package six.eared.macaque.http.codec.impl;

import six.eared.macaque.common.util.ReflectUtil;
import six.eared.macaque.http.codec.Decoder;

public abstract class BaseDecoder<Req> implements Decoder<Req> {

    protected final Class<Req> reqType;

    protected BaseDecoder(Class<Req> reqType) {
        this.reqType = reqType;
    }

    protected Req newReqObject() {
        return ReflectUtil.newInstance(reqType);
    }
}
