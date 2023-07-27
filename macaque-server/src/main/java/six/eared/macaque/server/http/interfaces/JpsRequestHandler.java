package six.eared.macaque.server.http.interfaces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import six.eared.macaque.http.annotitions.Path;
import six.eared.macaque.mbean.rmi.RmiResult;
import six.eared.macaque.server.http.ServerHttpInterface;
import six.eared.macaque.server.process.JavaProcessHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Path("jps")
public class JpsRequestHandler extends ServerHttpInterface<Object> {

    private static final Logger log = LoggerFactory.getLogger(JpsRequestHandler.class);

    @Override
    public RmiResult process0(Object n) {
        List<Map<String, String>> javaProcess = JavaProcessHolder.getJavaProcess().stream().map(item -> {
            Map<String, String> map = new HashMap<>();
            map.put("pid", item.getFirst());
            map.put("process", item.getSecond());
            return map;
        }).collect(Collectors.toList());

        log.info("jps process:[{}]", javaProcess);
        return RmiResult.success().data(javaProcess);
    }
}
