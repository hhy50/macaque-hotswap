package six.eared.macaque.core.client;

import six.eared.macaque.core.attach.Attach;
import six.eared.macaque.core.attach.DefaultAttachFactory;
import six.eared.macaque.core.common.AttachResultCode;
import six.eared.macaque.core.common.PortNumberGenerator;
import six.eared.macaque.core.common.PropertyName;
import six.eared.macaque.core.exception.JmxConnectException;
import six.eared.macaque.core.jmx.JmxClient;
import six.eared.macaque.core.jmx.JmxResourceManager;
import six.eared.macaque.mbean.MBean;
import six.eared.macaque.mbean.MBeanObjectName;
import six.eared.macaque.mbean.rmi.ClassHotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiData;
import six.eared.macaque.mbean.rmi.RmiResult;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MacaqueClient {

    private String agentPath;

    private JmxResourceManager jmxResourceManager = new JmxResourceManager();

    public MacaqueClient() {

    }

    public int attach(Integer pid) {
        if (!this.isAttached(pid)) {
            Attach runtimeAttach = DefaultAttachFactory.getInstance().createRuntimeAttach(pid);
            Integer jmxPort = PortNumberGenerator.getPort(pid);
            int attachCode = runtimeAttach.attach(this.agentPath, toPropertyString(jmxPort, true));
            switch (attachCode) {
                case AttachResultCode.SUCCESS:
                    JmxClient jmxClient = new JmxClient("127.0.0.1", jmxPort);
                    jmxResourceManager.addResource(pid, jmxClient);
                    break;
            }
            return attachCode;
        }
        return AttachResultCode.SUCCESS;
    }

    private boolean isAttached(Integer pid) {

        return false;
    }

    public RmiResult hotswap(Integer pid, ClassHotSwapRmiData data) throws Exception {
        RmiResult rmiResult = preHandler(pid);
        if (rmiResult != null) {
            return rmiResult;
        }

        JmxClient jmxClient = jmxResourceManager.getResource(pid);
        MBean<RmiData> processor = jmxClient.getMBean(MBeanObjectName.HOT_SWAP_MBEAN);
        return processor.process(data);
    }

    private RmiResult preHandler(Integer pid) {
        switch (attach(pid)) {
            case AttachResultCode.ERROR:
                return RmiResult.error(String.format("attach process '%s' fail", pid));
            case AttachResultCode.PROCESS_NOT_EXIST:
                return RmiResult.error(String.format("process pid='%d' not exist", pid));
        }

        RmiResult result = null;
        try {
            JmxClient jmxClient = jmxResourceManager.getResource(pid);
            if (!jmxClient.isConnect()) {
                jmxClient.disconnect();
                jmxClient.connect();
            }
        } catch (JmxConnectException e) {
            result = RmiResult.error(String.format("jmx connect error, pid='%d', msg=%s", pid, e.getMessage()));
        }
        return result;
    }


    protected String toPropertyString(int port, boolean debug) {
        Map<String, String> tmp = new HashMap<>();
        tmp.put(PropertyName.PORT, String.valueOf(port));
        tmp.put(PropertyName.DEBUG, Boolean.toString(debug));
        return tmp.entrySet().stream()
                .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(","));
    }

    public void setAgentPath(String agentPath) {
        this.agentPath = agentPath;
    }
}
