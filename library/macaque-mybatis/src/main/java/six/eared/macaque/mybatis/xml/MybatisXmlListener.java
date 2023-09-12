package six.eared.macaque.mybatis.xml;

import six.eared.macaque.library.hook.HotswapHook;
import six.eared.macaque.mbean.rmi.HotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;


public class MybatisXmlListener implements HotswapHook {

    @Override
    public RmiResult executeBefore(HotSwapRmiData rmiData) {
        System.out.println("executeBefore...");
        return null;
    }

    @Override
    public RmiResult executeAfter(HotSwapRmiData rmiData, RmiResult result, Throwable error) {
        System.out.println("executeAfter...");
        return null;
    }
}
