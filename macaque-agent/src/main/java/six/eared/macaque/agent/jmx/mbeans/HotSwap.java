package six.eared.macaque.agent.jmx.mbeans;

import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.agent.exceptions.HotswapException;
import six.eared.macaque.agent.hotswap.HandlerRegister;
import six.eared.macaque.agent.hotswap.handler.HotSwapHandler;
import six.eared.macaque.common.util.StringUtil;
import six.eared.macaque.mbean.MBeanObjectName;
import six.eared.macaque.mbean.rmi.HotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * 热加载MBean
 */
public class HotSwap implements HotSwapMBean {


    /**
     * 热加载
     *
     * @param request 热加载数据
     * @return 热加载结果
     */
    @Override
    public RmiResult process(HotSwapRmiData request) {
        String errMsg = null;
        try {
            if (request.getFileData() == null || request.getFileData().length == 0) {
                return RmiResult.error("filData is not be null");
            }
            if (StringUtil.isNotEmpty(request.getFileType())) {
                return RmiResult.error("fileType is not be null");
            }

            HotSwapHandler handler = HandlerRegister.getHandler(request.getFileType());
            return handler.handlerRequest(request);
        } catch (HotswapException e) {
            if (Environment.isDebug()) {
                e.printStackTrace();
            }
            errMsg = e.getDetails();
        }
        return RmiResult.error(errMsg);
    }

    @Override
    public ObjectName getMBeanName() throws MalformedObjectNameException {
        return new ObjectName(MBeanObjectName.HOT_SWAP_MBEAN);
    }
}
