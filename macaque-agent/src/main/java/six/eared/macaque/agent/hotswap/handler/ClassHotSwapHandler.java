package six.eared.macaque.agent.hotswap.handler;

import six.eared.macaque.agent.annotation.HotSwapFileType;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.asm2.classes.ClazzDefinitionVisitorFactory;
import six.eared.macaque.agent.asm2.classes.CompatibilityModeClassDefinitionVisitor;
import six.eared.macaque.agent.asm2.classes.MultiClassReader;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.common.ExtPropertyName;
import six.eared.macaque.common.type.FileType;
import six.eared.macaque.mbean.rmi.HotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;

import java.util.HashMap;
import java.util.Map;

import static six.eared.macaque.agent.hotswap.ClassHotSwapper.redefine;

@HotSwapFileType(fileType = FileType.Class)
public class ClassHotSwapHandler extends FileHookHandler {

    @Override
    public RmiResult doHandler(HotSwapRmiData rmiData) {
        Map<String, String> extProperties = rmiData.getExtProperties();
        byte[] bytes = rmiData.getFileData();
        return handler(bytes, extProperties);
    }

    public RmiResult handler(byte[] bytes, Map<String, String> extProperties) {
        String errMsg = null;
        try {
            Map<String, Object> result = new HashMap<>();

            ClazzDefinitionVisitorFactory factory = Boolean.TRUE.toString().equalsIgnoreCase(extProperties.get(ExtPropertyName.COMPATIBILITY_MODE))
                    ? new CompatibilityModeClassDefinitionVisitor()
                    : new ClazzDefinitionVisitorFactory.Default();
            MultiClassReader classReader = new MultiClassReader(bytes, factory);
            for (ClazzDefinition enhanced : classReader) {
                int redefineCount = redefine(enhanced);
                result.put(enhanced.getClassName(), redefineCount);
            }
            return RmiResult.success().data(result);
        } catch (Exception e) {
            if (Environment.isDebug()) {
                System.out.println("classHotSwap error");
                e.printStackTrace();
            }
            errMsg = e.getMessage();
            errMsg = errMsg == null ? e.toString() : errMsg;
        }
        return RmiResult.error(errMsg);
    }
}
