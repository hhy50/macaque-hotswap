package six.eared.macaque.agent.hotswap.handler;

import six.eared.macaque.agent.annotation.HotSwapFileType;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.asm2.classes.ClazzDefinitionVisitorFactory;
import six.eared.macaque.agent.asm2.enhance.CompatibilityModeByteCodeEnhancer;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.agent.vcs.VersionChainTool;
import six.eared.macaque.agent.vcs.VersionView;
import six.eared.macaque.common.ExtPropertyName;
import six.eared.macaque.common.type.FileType;
import six.eared.macaque.mbean.rmi.HotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;

import java.util.HashMap;
import java.util.List;
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

    @SuppressWarnings("unchecked")
    public RmiResult handler(byte[] bytes, Map<String, String> extProperties) {
        String errMsg = null;
        try {
            Map<String, Object> result = new HashMap<>();
            VersionView versionView = VersionChainTool.getActiveVersionView();
            boolean compatibilityMode = Boolean.TRUE.toString().equalsIgnoreCase(extProperties.get(ExtPropertyName.COMPATIBILITY_MODE));

            List<ClazzDefinition> definitions = AsmUtil.readMultiClass(bytes, compatibilityMode
                    ? ClazzDefinitionVisitorFactory.COMPATIBILITY_MODE
                    : ClazzDefinitionVisitorFactory.DEFAULT);
            versionView.setDefinitions((List) definitions);
            for (ClazzDefinition definition : definitions) {
                if (compatibilityMode) {
                    byte[] enhance = CompatibilityModeByteCodeEnhancer.enhance(definition.getByteCode());
                    definition.setByteCode(enhance);
                }
                int redefineCount = redefine(definition);
                result.put(definition.getClassName(), redefineCount);
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
