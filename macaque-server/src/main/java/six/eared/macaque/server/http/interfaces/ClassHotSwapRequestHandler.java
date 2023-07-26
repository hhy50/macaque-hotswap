package six.eared.macaque.server.http.interfaces;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import six.eared.macaque.http.annotitions.Path;
import six.eared.macaque.mbean.MBean;
import six.eared.macaque.mbean.MBeanObjectName;
import six.eared.macaque.mbean.rmi.ClassHotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;
import six.eared.macaque.server.config.LoggerName;
import six.eared.macaque.server.http.ServerHttpInterface;
import six.eared.macaque.server.http.model.ClassHotSwapDto;
import six.eared.macaque.server.jmx.JmxClient;
import six.eared.macaque.server.jmx.JmxClientResourceManager;
import six.eared.macaque.server.service.MacaqueServer;

import static six.eared.macaque.common.util.HexStringUtil.hex2binary;

@Path("/hotSwap")
public class ClassHotSwapRequestHandler extends ServerHttpInterface<ClassHotSwapDto> {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.auto());

    public ClassHotSwapRequestHandler(MacaqueServer macaqueServer) {
        super(macaqueServer);
    }

    @Override
    public RmiResult process0(ClassHotSwapDto dto) {
        String className = dto.getClassName();
        Integer pid = dto.getPid();

        if (attach(pid)) {
            JmxClient jmxClient = JmxClientResourceManager.getInstance()
                    .getResource(String.valueOf(pid));
            if (jmxClient == null) {
                MBean<ClassHotSwapRmiData> hotSwapMBean = jmxClient.getMBean(MBeanObjectName.HOT_SWAP_MBEAN);
                return hotSwapMBean.process(new ClassHotSwapRmiData(className, hex2binary(dto.getNewClassData())));
            }
        }
        return RmiResult.error("attach error");
    }
}
