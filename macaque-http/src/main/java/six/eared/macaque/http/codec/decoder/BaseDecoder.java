package six.eared.macaque.http.codec.decoder;

import six.eared.macaque.common.util.ReflectUtil;

public abstract class BaseDecoder<Req> implements Decoder<Req> {


    @SuppressWarnings("unchecked")
    protected Req newReqObject(Class<Req> reqType) {
        if (reqType != null) {
            return (Req) ReflectUtil.newInstance(reqType);
        }
        return null;
    }
}
