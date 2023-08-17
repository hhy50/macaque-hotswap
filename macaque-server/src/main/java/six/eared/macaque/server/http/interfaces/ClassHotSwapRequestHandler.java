package six.eared.macaque.server.http.interfaces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import six.eared.macaque.client.attach.Attach;
import six.eared.macaque.client.attach.DefaultAttachFactory;
import six.eared.macaque.client.common.PortNumberGenerator;
import six.eared.macaque.client.jmx.JmxClient;
import six.eared.macaque.client.jmx.JmxClientResourceManager;
import six.eared.macaque.common.util.StringUtil;
import six.eared.macaque.http.annotitions.Path;
import six.eared.macaque.http.annotitions.RequestMethod;
import six.eared.macaque.http.request.MultipartFile;
import six.eared.macaque.mbean.MBean;
import six.eared.macaque.mbean.MBeanObjectName;
import six.eared.macaque.mbean.rmi.ClassHotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;
import six.eared.macaque.server.config.ServerConfig;
import six.eared.macaque.server.http.ServerHttpInterface;
import six.eared.macaque.server.http.body.ClassHotSwapRequest;


@Path(value = "/hotSwap", method = RequestMethod.POST)
public class ClassHotSwapRequestHandler extends ServerHttpInterface<ClassHotSwapRequest> {

    private static final Logger log = LoggerFactory.getLogger(ClassHotSwapRequestHandler.class);

    private final DefaultAttachFactory defaultAttachFactory;

    private final ServerConfig serverConfig;

    public ClassHotSwapRequestHandler(DefaultAttachFactory defaultAttachFactory, ServerConfig serverConfig) {
        this.defaultAttachFactory = defaultAttachFactory;
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
        if (attach(pid)) {
            JmxClient jmxClient = JmxClientResourceManager.getInstance()
                    .getResource(String.valueOf(pid));
            if (jmxClient != null) {
                MBean<ClassHotSwapRmiData> hotSwapMBean = jmxClient.getMBean(MBeanObjectName.HOT_SWAP_MBEAN);
                RmiResult result = null;
                try {
                    result = hotSwapMBean.process(new ClassHotSwapRmiData(fileName, fileType, fileData.getBytes()));
                    log.info("ClassHotSwap pid:[{}] result:[{}]", pid, result);
                    return result;
                } catch (Exception e) {
                    log.error("ClassHotSwap error", e);
                    return RmiResult.error(e.getMessage());
                }
            }
        }
        return RmiResult.error("attach error");
    }

    protected boolean attach(Integer pid) {
        Attach runtimeAttach
                = this.defaultAttachFactory.createRuntimeAttach(String.valueOf(pid));

        Integer agentPort = PortNumberGenerator.getPort(pid);
        String property = String.format("port=%s,debug=%s", agentPort, Boolean.toString(this.serverConfig.isDebug()));
        return runtimeAttach.attach(this.serverConfig.getAgentpath(), property);
    }
}
