package six.eared.macaque.spring;


import six.eared.macaque.library.annotation.Library;
import six.eared.macaque.library.hook.HotswapHook;
import six.eared.macaque.mbean.rmi.HotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;

@Library(name = "spring", hooks = SpringLibraryConfiguration.class)
public class SpringLibraryConfiguration implements HotswapHook {

    @Override
    public RmiResult executeBefore(HotSwapRmiData rmiData) {
        return null;
    }

    @Override
    public RmiResult executeAfter(HotSwapRmiData rmiData, RmiResult result, Throwable error) {
        return null;
    }
}
