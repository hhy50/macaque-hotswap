package six.eared.macaque.agent.vcs;

import six.eared.macaque.agent.definition.FileDefinition;
import six.eared.macaque.common.type.FileType;
import six.eared.macaque.library.hook.HotswapHook;
import six.eared.macaque.mbean.rmi.HotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;


public class VersionChainRecorder implements HotswapHook {

    @Override
    public RmiResult executeBefore(HotSwapRmiData rmiData) {
        VersionView versionView = VersionChainTool.startNewEpoch();
        if (!FileType.Class.match(rmiData.getFileType())) {
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
