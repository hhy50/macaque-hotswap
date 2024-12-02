package six.eared.macaque.mybatis.xml;


import six.eared.macaque.library.hook.HotswapHook;
import six.eared.macaque.mbean.rmi.HotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import javax.xml.bind.annotation.XmlElement;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class MybatisXmlListener implements HotswapHook {

    @Override
    public RmiResult executeBefore(HotSwapRmiData rmiData) {
//        byte[] xmlData = rmiData.getFileData();
//        try (ByteArrayInputStream in = new ByteArrayInputStream(xmlData)) {
//            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
//            xmlReader.parse(new InputSource(in));
//            String namespace = (String) xmlReader.getProperty("namespace");
//
//        } catch (IOException | SAXException e) {
//            throw new RuntimeException(e);
//        }
//        return RmiResult.success();


        return null;
    }

    @Override
    public RmiResult executeAfter(HotSwapRmiData rmiData, RmiResult result, Throwable error) {
        return null;
    }
}
