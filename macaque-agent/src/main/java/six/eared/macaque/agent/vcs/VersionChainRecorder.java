package six.eared.macaque.agent.vcs;

import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.asm2.classes.ClazzDefinitionVisitorFactory;
import six.eared.macaque.agent.asm2.enhance.CompatibilityModeByteCodeEnhancer;
import six.eared.macaque.agent.definition.FileDefinition;
import six.eared.macaque.common.ExtPropertyName;
import six.eared.macaque.common.type.FileType;
import six.eared.macaque.library.hook.HotswapHook;
import six.eared.macaque.mbean.rmi.HotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;

import java.util.List;
import java.util.Map;


public class VersionChainRecorder implements HotswapHook {

    @Override
    @SuppressWarnings("unchecked")
    public RmiResult executeBefore(HotSwapRmiData rmiData) {
        VersionView versionView = VersionChainTool.startNewEpoch();
        Map<String, String> extProperties = rmiData.getExtProperties();
        if (FileType.Class.match(rmiData.getFileType())) {
            boolean compatibilityMode = Boolean.TRUE.toString().equalsIgnoreCase(extProperties.get(ExtPropertyName.COMPATIBILITY_MODE));
            List<ClazzDefinition> definitions = AsmUtil.readMultiClass(rmiData.getFileData(),
                    compatibilityMode ? ClazzDefinitionVisitorFactory.COMPATIBILITY_MODE
                            : ClazzDefinitionVisitorFactory.DEFAULT);
            versionView.setDefinitions((List) definitions);
            if (compatibilityMode) {
                for (ClazzDefinition definition : definitions) {
                    byte[] enhance = CompatibilityModeByteCodeEnhancer.enhance(definition.getByteCode());
                    definition.setByteCode(enhance);
                }
            }
        } else {
            FileDefinition fileDefinition = new FileDefinition();
            fileDefinition.setName(rmiData.getFileName());
            fileDefinition.setFileType(rmiData.getFileType());
            fileDefinition.setBytes(rmiData.getFileData());
            versionView.addDefinition(fileDefinition);
        }
        return null;
    }

    @Override
    public RmiResult executeAfter(HotSwapRmiData rmiData, RmiResult result, Throwable error) {
        if (VersionChainTool.inActiveVersionView()) {
            VersionChainTool.stopActiveVersionView(error != null);
        }
        return null;
    }
}
