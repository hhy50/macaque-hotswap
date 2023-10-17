package six.eared.macaque.agent.hotswap.handler;

import six.eared.macaque.agent.annotation.HotSwapFileType;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.asm2.classes.ClazzDefinitionVisitorFactory;
import six.eared.macaque.agent.asm2.enhance.CompatibilityModeByteCodeEnhancer;
import six.eared.macaque.agent.hotswap.ClassHotSwapper;
import six.eared.macaque.agent.vcs.VersionChainTool;
import six.eared.macaque.agent.vcs.VersionView;
import six.eared.macaque.common.ExtPropertyName;
import six.eared.macaque.common.type.FileType;
import six.eared.macaque.mbean.rmi.HotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@HotSwapFileType(fileType = FileType.Class)
public class ClassHotSwapHandler extends FileHookHandler {

    @Override
    public RmiResult doHandler(HotSwapRmiData rmiData) throws Exception {
        return handler(rmiData.getFileData(), rmiData.getExtProperties());
    }

    @SuppressWarnings("unchecked")
    public RmiResult handler(byte[] bytes, Map<String, String> extProperties) throws Exception {
        Map<String, Object> result = new HashMap<>();
        VersionView versionView = VersionChainTool.getActiveVersionView();

        List<ClazzDefinition> definitions = AsmUtil.readMultiClass(bytes, ClazzDefinitionVisitorFactory.DEFAULT);
        boolean compatibilityMode = Boolean.TRUE.toString()
                .equalsIgnoreCase(extProperties.get(ExtPropertyName.COMPATIBILITY_MODE));
        if (compatibilityMode) {
            definitions = CompatibilityModeByteCodeEnhancer
                    .enhance(definitions);
        }

        versionView.setDefinitions((List) definitions);
        for (ClazzDefinition definition : definitions) {
            if (compatibilityMode) {
                byte[] enhance = CompatibilityModeByteCodeEnhancer.enhance(definition.getByteCode());
                definition.setByteCode(enhance);
            }
            int redefineCount = ClassHotSwapper.redefine(definition);
            result.put(definition.getClassName(), redefineCount);
        }
        return RmiResult.success().data(result);
    }
}
