package six.eared.macaque.mybatis;


import io.github.hhy50.linker.LinkerFactory;
import io.github.hhy50.linker.annotations.Method;
import io.github.hhy50.linker.annotations.Target;
import io.github.hhy50.linker.exceptions.LinkerException;
import six.eared.macaque.agent.tool.VmToolExt;
import six.eared.macaque.library.hook.HotswapHook;
import six.eared.macaque.mbean.rmi.HotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;
import six.eared.macaque.mybatis.mapping.MybatisConfigure;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class MybatisXmlListener implements HotswapHook {

    @Override
    public RmiResult executeBefore(HotSwapRmiData rmiData) {
        if (!rmiData.getFileType().equals("xml")) {
            return null;
        }
        if (!isMapperXml(rmiData.getFileData())) {
            return null;
        }

        Object[] configureObjs = VmToolExt.getInstanceByName("org.apache.ibatis.session.Configuration");
        for (Object configureObj : configureObjs) {
            try {
                replaceXml(LinkerFactory.createLinker(MybatisConfigure.class, configureObj), rmiData.getFileData());
            } catch (LinkerException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private boolean isMapperXml(byte[] xmlData) {
        boolean hasMapperDoc = false;
        try (InputStream in = new ByteArrayInputStream(xmlData)) {
            XMLEventReader reader = XMLInputFactory.newInstance()
                    .createXMLEventReader(in);
            while (reader.hasNext()) {
                XMLEvent next = reader.nextEvent();
                if (next.getClass().getSimpleName().equals("StartElementEvent")
                        && LinkerFactory.createLinker(StartElementEvent.class, next)
                        .getName().equals(QName.valueOf("mapper"))
                ) {
                   hasMapperDoc = true;
                   break;
                }
            }
        } catch (Exception e) {

        }
        return hasMapperDoc;
    }

    private void replaceXml(MybatisConfigure configure, byte[] xmlData) {

    }

    private void doReplaceXml(MybatisConfigure linker) {
    }

    @Override
    public RmiResult executeAfter(HotSwapRmiData rmiData, RmiResult result, Throwable error) {
        return null;
    }

    @Target.Bind("com.sun.xml.internal.stream.events.StartElementEvent")
    static interface StartElementEvent {
        @Method.Name("getName")
        public QName getName();
    }
}
