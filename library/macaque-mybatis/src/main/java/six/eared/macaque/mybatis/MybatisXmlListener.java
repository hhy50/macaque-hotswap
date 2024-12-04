package six.eared.macaque.mybatis;


import io.github.hhy50.linker.LinkerFactory;
import lombok.SneakyThrows;
import six.eared.macaque.agent.tool.VmToolExt;
import six.eared.macaque.library.hook.HotswapHook;
import six.eared.macaque.mbean.rmi.HotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;


public class MybatisXmlListener implements HotswapHook {

    @SneakyThrows
    @Override
    public RmiResult executeBefore(HotSwapRmiData rmiData) {
        byte[] xmlData = rmiData.getFileData();
//        try (ByteArrayInputStream in = new ByteArrayInputStream(xmlData)) {
//            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
//            xmlReader.parse(new InputSource(in));
//            String namespace = (String) xmlReader.getProperty("namespace");
//
//        } catch (IOException | SAXException e) {
//            throw new RuntimeException(e);
//        }
//        return RmiResult.success();

//        VmTool instance = VmTool.getInstance();
//        VmTool instance = VmTool.getInstance("/Users/hanhaiyang/IdeaProjects/macaque-hotswap/macaque-agent/build/resources/main/libArthasJniLibrary.dylib");
        Object[] configureObjs = VmToolExt.getInstanceByName("org.apache.ibatis.session.Configuration");
        for (Object configureObj : configureObjs) {
            replaceXml(LinkerFactory.createLinker(MybatisConfigure.class, configureObj), xmlData);
        }
        return null;
    }

    private void replaceXml(MybatisConfigure configure, byte[] xmlData) {

    }

    private void doReplaceXml(MybatisConfigure linker) {
    }

    @Override
    public RmiResult executeAfter(HotSwapRmiData rmiData, RmiResult result, Throwable error) {
        return null;
    }
}
