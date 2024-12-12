package six.eared.macaque.mybatis;


import io.github.hhy50.linker.LinkerFactory;
import io.github.hhy50.linker.annotations.Method;
import io.github.hhy50.linker.annotations.Target;
import io.github.hhy50.linker.define.provider.DefaultTargetProviderImpl;
import six.eared.macaque.agent.tool.VmToolExt;
import six.eared.macaque.agent.vcs.VersionChainTool;
import six.eared.macaque.common.util.ReflectUtil;
import six.eared.macaque.library.hook.HotswapHook;
import six.eared.macaque.mbean.rmi.HotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;
import six.eared.macaque.mybatis.mapping.MybatisConfigure;
import six.eared.macaque.mybatis.mapping.XMLMapperBuilder;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class MybatisXmlListener implements HotswapHook {

    @Override
    public RmiResult executeBefore(HotSwapRmiData rmiData) {
        if (!rmiData.getFileType().equals("xml")) {
            return null;
        }
        String namespace = null;
        if ((namespace = getNamespace(rmiData.getFileData())) == null) {
            return null;
        }

        Object[] configureObjs = VmToolExt.getInstanceByName("org.apache.ibatis.session.Configuration");
        for (Object configureObj : configureObjs) {
            try {
                replaceXml(LinkerFactory.createLinker(MybatisConfigure.class, configureObj), namespace, rmiData.getFileData());
            } catch (Exception e) {
                return RmiResult.error(e.getMessage());
            }
        }
        return RmiResult.success();
    }

    private String getNamespace(byte[] xmlData) {
        try (InputStream in = new ByteArrayInputStream(xmlData)) {
            XMLEventReader reader = XMLInputFactory.newInstance()
                    .createXMLEventReader(in);
            while (reader.hasNext()) {
                XMLEvent next = reader.nextEvent();
                if (next.getClass().getSimpleName().equals("StartElementEvent")) {
                    StartElementEvent mapperEvent = LinkerFactory.createLinker(StartElementEvent.class, next);
                    if (mapperEvent.getName().equals(QName.valueOf("mapper"))) {
                        Attribute attr = mapperEvent.getAttributeByName(QName.valueOf("namespace"));
                        if (attr != null) {
                            return attr.getValue();
                        }
                    }
                }
            }
        } catch (Exception ignored) {

        }
        return null;
    }

    private void replaceXml(MybatisConfigure configure, String namespace, byte[] xmlData) throws MapperReplaceException {
        try (InputStream in = new ByteArrayInputStream(xmlData)) {
            Object o = ReflectUtil.newInstance(Class.forName("org.apache.ibatis.builder.xml.XMLMapperBuilder"),
                    in,
                    ((DefaultTargetProviderImpl) configure).getTarget(),
                    String.format("/macaque/%s/UserMapper.xml", VersionChainTool.getActiveVersionView().getVersion().getNumber()),
                    configure.getSqlFragments(),
                    namespace);
            LinkerFactory.createLinker(XMLMapperBuilder.class, o)
                    .parse();
        } catch (Exception e) {
            throw new MapperReplaceException(e.getMessage());
        }
    }

    @Override
    public RmiResult executeAfter(HotSwapRmiData rmiData, RmiResult result, Throwable error) {
        return null;
    }

    @Target.Bind("com.sun.xml.internal.stream.events.StartElementEvent")
    static interface StartElementEvent {
        @Method.Name("getName")
        public QName getName();

        @Method.Name("getAttributeByName")
        Attribute getAttributeByName(QName name);
    }
}
