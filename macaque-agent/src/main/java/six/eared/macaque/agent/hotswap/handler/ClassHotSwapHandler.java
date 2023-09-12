package six.eared.macaque.agent.hotswap.handler;

import six.eared.macaque.agent.annotation.HotSwapFileType;
import six.eared.macaque.agent.asm2.Enhancer;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.asm2.classes.MultiClassReader;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.agent.spi.LibrarySpiLoader;
import six.eared.macaque.common.type.FileType;
import six.eared.macaque.mbean.rmi.HotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static six.eared.macaque.agent.hotswap.ClassHotSwapper.redefine;

@HotSwapFileType(fileType = FileType.Class)
public class ClassHotSwapHandler extends FileHookHandler {

    private Iterator<Enhancer> enhancerIterator = LibrarySpiLoader.loadService(Enhancer.class);

    @Override
    public RmiResult doHandler(HotSwapRmiData rmiData) {
        return handler(rmiData.getFileData());
    }

    public RmiResult handler(byte[] bytes) {
        String errMsg = null;
        try {
            Map<String, Object> result = new HashMap<>();
            MultiClassReader classReader = new MultiClassReader(bytes);
            Iterator<ClazzDefinition> iterator = classReader.iterator();
            while (iterator.hasNext()) {
                ClazzDefinition enhanced = enhance(iterator.next());
                int redefineCount = redefine(enhanced);
                result.put(enhanced.getClassName(), redefineCount);
            }
            return RmiResult.success().data(result);
        } catch (Exception e) {
            if (Environment.isDebug()) {
                System.out.println("ClassHotSwap error");
                e.printStackTrace();
            }
            errMsg = e.getMessage();
            errMsg = errMsg == null ? e.toString() : errMsg;
        }
        return RmiResult.error(errMsg);
    }


    public ClazzDefinition enhance(ClazzDefinition definition) {
        ClazzDefinition origin = definition.clone();
        ClazzDefinition enhanced = null;
        while (enhancerIterator.hasNext()) {
            if (enhanced == null) {
                enhanced = definition;
            }
            Enhancer enhancer = enhancerIterator.next();
            enhanced = enhancer.enhance(enhanced);
        }

        if (enhanced == null) {
            enhanced = origin;
        }
        return enhanced;
    }
}
