package six.eared.macaque.server.http.interfaces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import six.eared.macaque.client.c.MacaqueClient;
import six.eared.macaque.common.util.StringUtil;
import six.eared.macaque.http.annotitions.Path;
import six.eared.macaque.http.annotitions.RequestMethod;
import six.eared.macaque.http.request.MultipartFile;
import six.eared.macaque.mbean.rmi.ClassHotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;
import six.eared.macaque.server.config.ServerConfig;
import six.eared.macaque.server.http.ServerHttpInterface;
import six.eared.macaque.server.http.body.ClassHotSwapRequest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Path(value = "/hotSwap", method = RequestMethod.POST)
public class ClassHotSwapRequestHandler extends ServerHttpInterface<ClassHotSwapRequest> {

    private static final Logger log = LoggerFactory.getLogger(ClassHotSwapRequestHandler.class);

    private static final Map<Integer, MacaqueClient> CLIENTS = new ConcurrentHashMap<>();

    private final ServerConfig serverConfig;

    public ClassHotSwapRequestHandler(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    @Override
    public RmiResult process0(ClassHotSwapRequest dto) {
        Integer pid = dto.getPid();
        String fileType = dto.getFileType();
        String fileName = dto.getFileName();
        MultipartFile fileData = dto.getFileData();

        if (pid == null
                || StringUtil.isEmpty(fileType)
                || fileData == null || fileData.getBytes() == null) {
            log.error("ClassHotSwap error, params not be null");
            return RmiResult.error("error");
        }
        MacaqueClient client = getClient(pid);
        try {
            RmiResult result = client.hotswap(new ClassHotSwapRmiData(fileName, fileType, fileData.getBytes()));
            log.info("ClassHotSwap pid:[{}] result:[{}]", pid, result);
            return result;
        } catch (Exception e) {
            log.error("hotswap error", e);
        }
        return RmiResult.error("attach error");
    }

    public MacaqueClient getClient(Integer port) {
        MacaqueClient client = CLIENTS.get(port);
        if (client != null) {
            return client;
        }
        client = new MacaqueClient(port);
        client.setAgentPath(this.serverConfig.getAgentpath());
        CLIENTS.put(port, client);
        return client;
    }
}
