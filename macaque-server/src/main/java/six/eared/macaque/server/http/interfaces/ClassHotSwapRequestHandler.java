package six.eared.macaque.server.http.interfaces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import six.eared.macaque.common.util.StringUtil;
import six.eared.macaque.http.annotitions.Path;
import six.eared.macaque.http.request.MultipartFile;
import six.eared.macaque.mbean.MBean;
import six.eared.macaque.mbean.MBeanObjectName;
import six.eared.macaque.mbean.rmi.ClassHotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;
import six.eared.macaque.server.attach.Attach;
import six.eared.macaque.server.attach.DefaultAttachFactory;
import six.eared.macaque.server.http.ServerHttpInterface;
import six.eared.macaque.server.http.model.ClassHotSwapDto;
import six.eared.macaque.server.jmx.JmxClient;
import six.eared.macaque.server.jmx.JmxClientResourceManager;


@Path("/hotSwap")
public class ClassHotSwapRequestHandler extends ServerHttpInterface<ClassHotSwapDto> {

    private static final Logger log = LoggerFactory.getLogger(ClassHotSwapRequestHandler.class);
    private final DefaultAttachFactory defaultAttachFactory;

    public ClassHotSwapRequestHandler(DefaultAttachFactory defaultAttachFactory) {
        this.defaultAttachFactory = defaultAttachFactory;
    }

    @Override
    public RmiResult process0(ClassHotSwapDto dto) {
        String className = dto.getClassName();
        Integer pid = dto.getPid();
        MultipartFile newClassData = dto.getNewClassData();

        if (StringUtil.isEmpty(className) || pid == null
                || newClassData == null || newClassData.getBytes() == null) {
            log.error("ClassHotSwap error, params not be null");
            return RmiResult.error("error");
        }

        log.info("ClassHotSwap pid:[{}] className:[{}]", pid, className);
        if (attach(pid)) {
            JmxClient jmxClient = JmxClientResourceManager.getInstance()
                    .getResource(String.valueOf(pid));
            if (jmxClient != null) {
                MBean<ClassHotSwapRmiData> hotSwapMBean = jmxClient.getMBean(MBeanObjectName.HOT_SWAP_MBEAN);
                RmiResult result = null;
                try {
                    result = hotSwapMBean.process(new ClassHotSwapRmiData(className, newClassData.getBytes()));
                } catch (Exception e) {
                    log.error("ClassHotSwap error", e);
                }
                log.info("ClassHotSwap pid:[{}] className:[{}], result:[{}]", pid, className, result);
                return result;
            }
            log.error("attach error, jmxClient is null");
        }
        return RmiResult.error("attach error");
    }

    protected boolean attach(Integer pid) {
        Attach runtimeAttach
                = this.defaultAttachFactory.createRuntimeAttach(String.valueOf(pid));
        return runtimeAttach.attach();
    }
}
