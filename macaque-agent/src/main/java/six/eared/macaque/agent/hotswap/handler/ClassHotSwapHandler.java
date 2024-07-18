package six.eared.macaque.agent.hotswap.handler;

import six.eared.macaque.agent.annotation.HotSwapFileType;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.enhance.ClassIncrementUpdate;
import six.eared.macaque.agent.enhance.ClazzDataDefinition;
import six.eared.macaque.agent.enhance.CompatibilityModeByteCodeEnhancer;
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
        List<ClazzDataDefinition> definitions = AsmUtil.readMultiClass(bytes);

        boolean compatibilityMode = Boolean.TRUE.toString()
                .equalsIgnoreCase(extProperties.get(ExtPropertyName.COMPATIBILITY_MODE));

        if (compatibilityMode) {
            List<ClassIncrementUpdate> enhanced = CompatibilityModeByteCodeEnhancer.enhance(definitions);
            return RmiResult.success().data(ClassHotSwapper.redefines(flatClassDefinition2(enhanced)));
        } else {
            return RmiResult.success().data(ClassHotSwapper
                    .redefines(flatClassDefinition(definitions)));
        }
    }

    private Map<String, byte[]> flatClassDefinition2(List<ClassIncrementUpdate> enhanced) {
        VersionView versionView = VersionChainTool.getActiveVersionView();

        Map<String, byte[]> flatMap = new HashMap<>();
        for (ClassIncrementUpdate item : enhanced) {
            flatMap.put(item.getClassName(), item.getEnhancedByteCode());
//            versionView.addDefinition(item);
        }
        return flatMap;
    }

    private Map<String, byte[]> flatClassDefinition(List<ClazzDataDefinition> definitions) {
        VersionView versionView = VersionChainTool.getActiveVersionView();

        Map<String, byte[]> flatMap = new HashMap<>();
        for (ClazzDataDefinition item : definitions) {
            flatMap.put(item.getClassName(), item.getBytecode());
//            versionView.addDefinition(item);
        }
        return flatMap;
    }
}
