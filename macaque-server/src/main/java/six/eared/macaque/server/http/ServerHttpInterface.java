package six.eared.macaque.server.http;

import six.eared.macaque.common.rmi.RmiResult;
import six.eared.macaque.http.handler.BaseRequestHandler;

public abstract class ServerHttpInterface<T> extends BaseRequestHandler<T> {

    public ServerHttpInterface() {
        //ServerHttpInterfaceHolder.addInterface((Class<ServerHttpInterface>) this.getClass());
    }

    @Override
    public abstract RmiResult process0(T t);
}
