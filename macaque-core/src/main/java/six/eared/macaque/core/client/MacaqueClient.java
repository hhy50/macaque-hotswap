package six.eared.macaque.core.client;

import six.eared.macaque.core.attach.Attach;
import six.eared.macaque.core.attach.DefaultAttachFactory;
import six.eared.macaque.core.common.AttachResultCode;
import six.eared.macaque.core.common.PortNumberGenerator;
import six.eared.macaque.core.common.PropertyName;
import six.eared.macaque.common.util.StringUtil;
import six.eared.macaque.mbean.MBean;
import six.eared.macaque.mbean.MBeanObjectName;
import six.eared.macaque.mbean.rmi.ClassHotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiData;
import six.eared.macaque.mbean.rmi.RmiResult;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MacaqueClient {

    private Integer pid;

    private Integer defaultPort;

    private String agentPath;

    private final Map<String, String> properties = new ConcurrentHashMap<>();

    private JmxClient jmxClient;

    private volatile boolean attached;

    public MacaqueClient(Integer pid) {
        this.pid = pid;
        this.defaultPort = PortNumberGenerator.getPort(pid);
    }

    public int attach() {
        if (!this.attached) {
            Attach runtimeAttach = DefaultAttachFactory.getInstance().createRuntimeAttach(pid);
            int attachCode = runtimeAttach.attach(this.agentPath, toPropertyString());
            switch (attachCode) {
                case AttachResultCode.SUCCESS:
                    this.jmxClient = new JmxClient("127.0.0.1", this.getJmxPort());
                    this.jmxClient.connect();
                    this.attached = true;
                    break;
            }
            return attachCode;
        }
        return AttachResultCode.SUCCESS;
    }

    public RmiResult hotswap(ClassHotSwapRmiData data) throws Exception {
        RmiResult rmiResult = preHandler();
        if (rmiResult != null) {
            return rmiResult;
        }
        MBean<RmiData> processor = this.jmxClient.getMBean(MBeanObjectName.HOT_SWAP_MBEAN);
        return processor.process(data);
    }

    private RmiResult preHandler() {
        switch (attach()) {
            case AttachResultCode.ERROR:
                return RmiResult.error(String.format("attach '%s' fail", this.getJmxPort()));
            case AttachResultCode.PROCESS_NOT_EXIST:
                return RmiResult.error(String.format("process pid='%d' not exist", this.pid));
        }

        if (!this.jmxClient.isConnect()) {
            this.jmxClient.disconnect();
            this.jmxClient.connect();
        }

        return null;
    }

    public void addProperty(String name, String value) {
        if (!attached) {
            properties.put(name, value);
            return;
        }
        throw new RuntimeException("attached, not support addProperty");
    }

    protected String toPropertyString() {
        Map<String, String> tmp = new HashMap<>(properties);

        if (!tmp.containsKey(PropertyName.PORT)) {
            tmp.put(PropertyName.PORT, String.valueOf(this.defaultPort));
        }
        if (!tmp.containsKey(PropertyName.DEBUG)) {
            tmp.put(PropertyName.DEBUG, Boolean.TRUE.toString());
        }
        return tmp.entrySet().stream()
                .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(","));
    }

    public void setAgentPath(String agentPath) {
        this.agentPath = agentPath;
    }

    public Integer getJmxPort() {
        String port = this.properties.get(PropertyName.PORT);
        if (StringUtil.isEmpty(port)) {
            return this.defaultPort;
        }
        return Integer.parseInt(port);
    }
}
