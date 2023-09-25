package six.eared.macaque.agent.hotswap.handler;

import six.eared.macaque.agent.annotation.HotSwapFileType;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.agent.vcs.VersionChainTool;
import six.eared.macaque.agent.vcs.VersionView;
import six.eared.macaque.common.type.FileType;
import six.eared.macaque.mbean.rmi.HotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

            VersionView versionView = VersionChainTool.getActiveVersionView();
            List<ClazzDefinition> definitions = versionView.getDefinitions().stream()
                    .map(ClazzDefinition.class::cast)
                    .collect(Collectors.toList());

            for (ClazzDefinition definition : definitions) {
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
