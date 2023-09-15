package six.eared.macaque.agent.hotswap.handler;


import six.eared.macaque.mbean.rmi.HotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;

public interface HotSwapHandler {


    /**
     *
     * @param rmiData
     * @return
     */
    public RmiResult handlerRequest(HotSwapRmiData rmiData);
}
