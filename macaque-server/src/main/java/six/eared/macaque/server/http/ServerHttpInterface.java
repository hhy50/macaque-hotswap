package six.eared.macaque.server.http;

import six.eared.macaque.http.handler.BaseRequestHandler;
import six.eared.macaque.mbean.rmi.RmiResult;
import six.eared.macaque.server.attach.Attach;
import six.eared.macaque.server.service.MacaqueServer;

public abstract class ServerHttpInterface<T> extends BaseRequestHandler<T> {

    private MacaqueServer macaqueServer;

    public ServerHttpInterface(MacaqueServer macaqueServer) {
        this.macaqueServer = macaqueServer;
    }

    protected boolean attach(Integer pid) {
        Attach runtimeAttach = macaqueServer.getDefaultAttachFactory()
                .createRuntimeAttach(String.valueOf(pid));
        return runtimeAttach.attach();
    }

    @Override
    public abstract RmiResult process0(T t);
}
