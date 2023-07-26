package six.eared.macaque.server.attach;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import six.eared.macaque.server.common.PortNumberGenerator;
import six.eared.macaque.server.config.LoggerName;
import six.eared.macaque.server.config.ServerConfig;
import six.eared.macaque.server.jmx.JmxClientResourceManager;

import java.io.IOException;

public class RuntimeAttach implements Attach {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.auto());

    private String pid;

    private final ServerConfig config;

    private VirtualMachine targetVM;

    private volatile boolean attached;

    public RuntimeAttach(String pid, ServerConfig config) {
        this.pid = pid;
        this.config = config;
    }

    @Override
    public synchronized boolean attach() {
        if (this.attached) {
            return true;
        }
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

            if (this.targetVM != null) {
                Integer agentPort = PortNumberGenerator.getPort(Integer.parseInt(pid));
                String property = String.format("port=%s,debug=%s", agentPort, Boolean.toString(this.config.isDebug()));
                this.targetVM.loadAgent(this.config.getAgentpath(), property);
                this.attached = JmxClientResourceManager.getInstance()
                        .createResource(pid) != null;
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
        return this.attached;
    }
}
