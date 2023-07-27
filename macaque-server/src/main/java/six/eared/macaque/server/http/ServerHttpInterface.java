package six.eared.macaque.server.http;

import six.eared.macaque.http.handler.BaseRequestHandler;
import six.eared.macaque.mbean.rmi.RmiResult;

public abstract class ServerHttpInterface<T> extends BaseRequestHandler<T> {


    @Override
    public abstract RmiResult process0(T t);
}
