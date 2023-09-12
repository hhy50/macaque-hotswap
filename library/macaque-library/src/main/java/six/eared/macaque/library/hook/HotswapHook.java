package six.eared.macaque.library.hook;

import six.eared.macaque.mbean.rmi.HotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;

public interface HotswapHook {

    /**
     *
     * @param rmiData
     * @return
     */
    public RmiResult executeBefore(HotSwapRmiData rmiData);


    /**
     *
     * @param rmiData
     * @param result
     * @return
     */
    public RmiResult executeAfter(HotSwapRmiData rmiData, RmiResult result, Throwable error);
}
