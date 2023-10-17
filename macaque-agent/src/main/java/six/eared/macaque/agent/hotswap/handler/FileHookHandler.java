package six.eared.macaque.agent.hotswap.handler;

import six.eared.macaque.agent.exceptions.HotswapException;
import six.eared.macaque.library.hook.HotswapHook;
import six.eared.macaque.mbean.rmi.HotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;

import java.util.ArrayList;
import java.util.List;


public abstract class FileHookHandler implements HotSwapHandler {

    private static final List<HotswapHook> HOOKS = new ArrayList<>();

    @Override
    public RmiResult handlerRequest(HotSwapRmiData rmiData) {
        Throwable error = null;
        RmiResult result = null;

        try {
            result = executeBeforeHook(rmiData);
            if (result != null) {
                return result;
            }
            result = doHandler(rmiData);
            if (result == null) {
                result = RmiResult.success();
            }
        } catch (Throwable e) {
            error = e;
        } finally {
            result = executeAfterHook(rmiData, result, error);
        }
        if (result != null) {
            return result;
        }
        throw new HotswapException(error);
    }

    protected RmiResult executeBeforeHook(HotSwapRmiData rmiData) throws Exception {
        for (HotswapHook hook : HOOKS) {
            RmiResult rewriteResult = hook.executeBefore(rmiData);
            if (rewriteResult != null) {
                return rewriteResult;
            }
        }
        return null;
    }

    protected RmiResult executeAfterHook(HotSwapRmiData rmiData, RmiResult originResult, Throwable error) {
        for (HotswapHook hook : HOOKS) {
            RmiResult rewriteResult = hook.executeAfter(rmiData, originResult, error);
            if (rewriteResult != null) {
                return rewriteResult;
            }
        }
        return originResult;
    }

    protected abstract RmiResult doHandler(HotSwapRmiData rmiData) throws Exception;

    public static void registerHook(HotswapHook hotswapHook) {
        HOOKS.add(hotswapHook);
    }
}
