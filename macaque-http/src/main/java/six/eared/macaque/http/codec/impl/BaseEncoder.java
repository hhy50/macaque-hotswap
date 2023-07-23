package six.eared.macaque.http.codec.impl;

import six.eared.macaque.common.util.ReflectUtil;
import six.eared.macaque.http.codec.Encoder;

public abstract class BaseEncoder<Res> implements Encoder<Res> {

    protected final Class<Res> resType;

    protected BaseEncoder(Class<Res> resType) {
        this.resType = resType;
    }

    protected Res newResObject() {
        return ReflectUtil.newInstance(resType);
    }
}
