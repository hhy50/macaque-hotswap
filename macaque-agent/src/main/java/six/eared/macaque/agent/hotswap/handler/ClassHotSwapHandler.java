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
        VersionView versionView = VersionChainTool.getActiveVersionView();

        List<ClazzDefinition> definitions = AsmUtil.readMultiClass(bytes, ClazzDefinitionVisitorFactory.DEFAULT);
        boolean compatibilityMode = Boolean.TRUE.toString()
                .equalsIgnoreCase(extProperties.get(ExtPropertyName.COMPATIBILITY_MODE));

        if (compatibilityMode) {
            for (ClazzDefinition definition : definitions) {
                CompatibilityModeByteCodeEnhancer
                        .enhance(definition);
                versionView.addDefinition(definition);
            }
        }

        return RmiResult.success().data(ClassHotSwapper
                .redefine(flatClassDefinition(definitions)));
    }

    private ClazzDefinition flatClassDefinition(List<ClazzDefinition> definitions) {


        return null;
    }
}
