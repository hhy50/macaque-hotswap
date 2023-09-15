package six.eared.macaque.server.http.interfaces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import six.eared.macaque.common.util.StringUtil;
import six.eared.macaque.core.client.MacaqueClient;
import six.eared.macaque.http.annotitions.Path;
import six.eared.macaque.http.annotitions.RequestMethod;
import six.eared.macaque.http.request.MultipartFile;
import six.eared.macaque.mbean.rmi.HotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;
import six.eared.macaque.server.http.ServerHttpInterface;
import six.eared.macaque.server.http.body.ClassHotSwapRequest;

import java.util.HashMap;


@Path(value = "/hotSwap", method = RequestMethod.POST)
public class ClassHotSwapRequestHandler extends ServerHttpInterface<ClassHotSwapRequest> {

    private static final Logger log = LoggerFactory.getLogger(ClassHotSwapRequestHandler.class);

    private final MacaqueClient macaqueClient;

    public ClassHotSwapRequestHandler(MacaqueClient macaqueClient) {
        this.macaqueClient = macaqueClient;
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
        try {
            RmiResult result = macaqueClient.hotswap(pid, new HotSwapRmiData(fileName, fileType, fileData.getBytes(), new HashMap<>()));
            log.info("ClassHotSwap pid:[{}] result:[{}]", pid, result);
            return result;
        } catch (Exception e) {
            log.error("hotswap error", e);
        }
        return RmiResult.error("attach error");
    }
}
