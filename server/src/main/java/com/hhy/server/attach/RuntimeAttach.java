package com.hhy.server.attach;

import com.hhy.server.config.ServerConfig;
import com.hhy.server.log.LoggerName;
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
    public void attach(String pid) {
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

            targetVM.loadAgent(config.getAgentpath(), config.getAgentProperties());
        } catch (Exception e) {
            log.error("attach error", e);
        }
    }
}
