package six.eared.macaque.agent.vcs;

import six.eared.macaque.library.hook.HotswapHook;
import six.eared.macaque.mbean.rmi.HotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;


public class VersionChainRecorder implements HotswapHook {

    @Override
    public RmiResult executeBefore(HotSwapRmiData rmiData) {
        VersionChainTool.startNewEpoch();
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
