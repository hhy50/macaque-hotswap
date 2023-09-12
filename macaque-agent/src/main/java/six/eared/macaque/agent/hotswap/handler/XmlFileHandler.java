package six.eared.macaque.agent.hotswap.handler;


import six.eared.macaque.agent.annotation.HotSwapFileType;
import six.eared.macaque.common.type.FileType;
import six.eared.macaque.mbean.rmi.HotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;



@HotSwapFileType(fileType = FileType.Xml)
public class XmlFileHandler extends FileHookHandler {

    @Override
    protected RmiResult doHandler(HotSwapRmiData rmiData) {
        return RmiResult.success();
    }
}
