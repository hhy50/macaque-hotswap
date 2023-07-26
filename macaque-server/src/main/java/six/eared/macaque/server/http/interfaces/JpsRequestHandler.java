package six.eared.macaque.server.http.interfaces;

import six.eared.macaque.http.annotitions.Path;
import six.eared.macaque.mbean.rmi.RmiResult;
import six.eared.macaque.server.http.ServerHttpInterface;
import six.eared.macaque.server.process.JavaProcessHolder;
import six.eared.macaque.server.service.MacaqueServer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Path("jps")
public class JpsRequestHandler extends ServerHttpInterface<Void> {

    public JpsRequestHandler(MacaqueServer macaqueServer) {
        super(macaqueServer);
    }

    @Override
    public RmiResult process0(Void v) {
        List<Map<String, String>> javaProcess = JavaProcessHolder.getJavaProcess().stream().map(item -> {
            Map<String, String> map = new HashMap<>();
            map.put("pid", item.getFirst());
            map.put("process", item.getSecond());
            return map;
        }).collect(Collectors.toList());

        return RmiResult.success().data(javaProcess);
    }
}
