package com.hhy.server.attach;

import com.hhy.server.common.PortNumberGenerator;
import com.hhy.server.config.ServerConfig;
import com.hhy.server.config.LoggerName;
import com.hhy.server.jmx.JmxClient;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuntimeAttach implements Attach {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.auto());

    private final ServerConfig config;

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

        VirtualMachine targetVM = null;
        try {
            if (virtualMachineDescriptor == null) {
                targetVM = VirtualMachine.attach(pid);
            } else {
                targetVM = VirtualMachine.attach(virtualMachineDescriptor);
            }

            Integer agentPort = PortNumberGenerator.getPort(Integer.parseInt(pid));
            String property = String.format("port=%s", agentPort);
            targetVM.loadAgent(config.getAgentpath(), property);

            JmxClient jmxClient = new JmxClient("127.0.0.1", agentPort);
            try {
                return jmxClient.connect();
            } finally {
                jmxClient.distory();
            }
        } catch (Exception e) {
            log.error("attach error", e);
        }
        return false;
    }

    @Override
    public boolean detach(String pid) {
        return false;
    }
}
