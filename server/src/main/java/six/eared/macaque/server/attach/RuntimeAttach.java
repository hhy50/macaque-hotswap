package six.eared.macaque.server.attach;

import six.eared.macaque.server.common.PortNumberGenerator;
import six.eared.macaque.server.config.ServerConfig;
import six.eared.macaque.server.config.LoggerName;
import six.eared.macaque.server.jmx.JmxClient;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RuntimeAttach implements Attach {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.auto());

    private final ServerConfig config;

    private VirtualMachine targetVM;

    public RuntimeAttach(ServerConfig config) {
        this.config = config;
    }

    @Override
    public boolean attach(String pid) {
        VirtualMachineDescriptor virtualMachineDescriptor = null;
        for (VirtualMachineDescriptor descriptor : VirtualMachine.list()) {
            if (descriptor.id().equals(pid)) {
                virtualMachineDescriptor = descriptor;
                break;
            }
        }

        try {
            if (virtualMachineDescriptor == null) {
                this.targetVM = VirtualMachine.attach(pid);
            } else {
                this.targetVM = VirtualMachine.attach(virtualMachineDescriptor);
            }

            Integer agentPort = PortNumberGenerator.getPort(Integer.parseInt(pid));
            String property = String.format("port=%s,debug=%s", agentPort, Boolean.toString(this.config.isDebug()));
            this.targetVM.loadAgent(this.config.getAgentpath(), property);

            JmxClient jmxClient = null;
            try {
                jmxClient = new JmxClient("127.0.0.1", agentPort);
                return jmxClient.connect();
            } finally {
                if (jmxClient != null) {
                    jmxClient.distory();
                }
            }
        } catch (Exception e) {
            log.error("attach error", e);
        } finally {
            if (targetVM != null) {
                try {
                    targetVM.detach();
                } catch (IOException e) {
                    log.error("detach error", e);
                }
            }
        }
        return false;
    }
}
