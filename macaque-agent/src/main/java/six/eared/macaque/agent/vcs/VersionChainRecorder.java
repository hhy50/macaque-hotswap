package six.eared.macaque.agent.vcs;

import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.asm2.classes.ClazzDefinitionVisitorFactory;
import six.eared.macaque.agent.definition.FileDefinition;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.common.ExtPropertyName;
import six.eared.macaque.common.type.FileType;
import six.eared.macaque.library.hook.HotswapHook;
import six.eared.macaque.mbean.rmi.HotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;

import java.util.List;
import java.util.Map;


public class VersionChainRecorder implements HotswapHook {

    @Override
    public RmiResult executeBefore(HotSwapRmiData rmiData) {
        if (Environment.isOpenVersionControl()) {
            Map<String, String> extProperties = rmiData.getExtProperties();

            VersionView versionView = VersionChainTool.startNewEpoch();
            if (FileType.Class.match(rmiData.getFileType())) {
                boolean compatibilityMode = Boolean.TRUE.toString().equalsIgnoreCase(extProperties.get(ExtPropertyName.COMPATIBILITY_MODE));
                List<ClazzDefinition> definitions = AsmUtil.readMultiClass(rmiData.getFileData(),
                        compatibilityMode ? ClazzDefinitionVisitorFactory.COMPATIBILITY_MODE
                                : ClazzDefinitionVisitorFactory.DEFAULT);
                for (ClazzDefinition definition : definitions) {
                    versionView.addDefinition(definition);
                }
            } else {
                FileDefinition fileDefinition = new FileDefinition();
                fileDefinition.setName(rmiData.getFileName());
                fileDefinition.setFileType(rmiData.getFileType());
                fileDefinition.setBytes(rmiData.getFileData());
                versionView.addDefinition(fileDefinition);
            }
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
