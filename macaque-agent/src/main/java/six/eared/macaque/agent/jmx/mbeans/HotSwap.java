package six.eared.macaque.agent.jmx.mbeans;

import six.eared.macaque.agent.compiler.java.JavaSourceCompiler;
import six.eared.macaque.agent.hotswap.HandlerRegister;
import six.eared.macaque.agent.hotswap.handler.HotSwapHandler;
import six.eared.macaque.common.type.FileType;
import six.eared.macaque.common.util.StringUtil;
import six.eared.macaque.mbean.MBeanObjectName;
import six.eared.macaque.mbean.rmi.HotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 热加载MBean
 */
public class HotSwap implements HotSwapMBean {

    private final JavaSourceCompiler compiler = new JavaSourceCompiler();

    /**
     * 热加载
     *
     * @param request 热加载数据
     * @return 热加载结果
     */
    @Override
    public RmiResult process(HotSwapRmiData request) {
        String fileType = request.getFileType();
        String fileName = request.getFileName();

        if (FileType.Java.match(fileType)) {
            if (StringUtil.isEmpty(fileType)) {
                return RmiResult.error("file type is java, file must not be null");
            }
            byte[] fileData = request.getFileData();

            Map<String, byte[]> sources = new HashMap<>();
            sources.put(fileName, fileData);
            List<byte[]> compiled = compiler.compile(sources);

            byte[] bytes = mergeClassData(compiled);
            request.setFileData(bytes);
            fileType = FileType.Class.getType();
        }

        HotSwapHandler handler = HandlerRegister.getHandler(fileType);
        return handler.handlerRequest(request);
    }

    public byte[] mergeClassData(List<byte[]> byteList) {
        return byteList.stream().reduce((b1, b2) -> {
            int len = b1.length + b2.length;
            byte[] bytes = new byte[len];

            System.arraycopy(b1, 0, bytes, 0, b1.length);
            System.arraycopy(b2, 0, bytes, b1.length, b2.length);
            return bytes;
        }).get();
    }

    @Override
    public ObjectName getMBeanName() throws MalformedObjectNameException {
        return new ObjectName(MBeanObjectName.HOT_SWAP_MBEAN);
    }
}
