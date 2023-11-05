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

// E:\dev\jdk1.8_361\bin\java.exe -agentlib:jdwp=transport=dt_socket,address=127.0.0.1:58350,suspend=y,server=n -XX:TieredStopAtLevel=1 -noverify -Dspring.output.ansi.enabled=always -Dcom.sun.management.jmxremote -Dspring.jmx.enabled=true -Dspring.liveBeansView.mbeanDomain -Dspring.application.admin.enabled=true -javaagent:D:\ideaIU-2023.1.3\plugins\java\lib\rt\debugger-agent.jar -Dfile.encoding=UTF-8 -classpath C:\Users\haiyang\AppData\Local\Temp\classpath1397808741.jar com.yuyuka.billiards.service.ServicesProvider
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
            if (StringUtil.isEmpty(request.getFileType())) {
                return RmiResult.error("fileType is not be null");
            }

            HotSwapHandler handler = HandlerRegister.getHandler(request.getFileType());
            if (handler == null) {
                throw new HotswapException("Not supported file type: " + request.getFileType());
            }
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
