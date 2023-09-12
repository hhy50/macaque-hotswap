package six.eared.macaque.agent.hotswap.handler;

import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.library.hook.HotswapHook;
import six.eared.macaque.mbean.rmi.HotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;

import java.util.ArrayList;
import java.util.List;


public abstract class FileHookHandler implements HotSwapHandler {

    private static final List<HotswapHook> HOOKS = new ArrayList<>();

    @Override
    public RmiResult handlerRequest(HotSwapRmiData rmiData) {
        RmiResult result = executeBeforeHook(rmiData);
        if (result != null) {
            return result;
        }

        Throwable error = null;
        try {
            result = doHandler(rmiData);
        } catch (Exception e) {
            error = e;
        } finally {

        }
        return executeAfterHook(rmiData, result, error);
    }

    protected RmiResult executeBeforeHook(HotSwapRmiData rmiData) {
        try {
            for (HotswapHook hook : HOOKS) {
                RmiResult rewriteResult = hook.executeBefore(rmiData);
                if (rewriteResult != null) {
                    return rewriteResult;
                }
            }
        } catch (Exception e) {
            if (Environment.isDebug()) {
                System.out.println("executeBeforeHoot error");
                e.printStackTrace();
            }
        }
        return null;
    }

    protected RmiResult executeAfterHook(HotSwapRmiData rmiData, RmiResult originResult, Throwable error) {
        try {
            for (HotswapHook hook : HOOKS) {
                RmiResult rewriteResult = hook.executeAfter(rmiData, originResult, error);
                if (rewriteResult != null) {
                    return rewriteResult;
                }
            }
        } catch (Exception e) {
            if (Environment.isDebug()) {
                System.out.println("executeAfterHoot error");
                e.printStackTrace();
            }
        }
        return originResult;
    }

    protected abstract RmiResult doHandler(HotSwapRmiData rmiData);

    public static void registerHook(HotswapHook hotswapHook) {
        HOOKS.add(hotswapHook);
    }
}
