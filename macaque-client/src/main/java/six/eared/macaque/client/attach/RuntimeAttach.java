package six.eared.macaque.client.attach;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import six.eared.macaque.client.jmx.JmxClientResourceManager;

import java.io.IOException;


public class RuntimeAttach implements Attach {

    private static final Logger log = LoggerFactory.getLogger(RuntimeAttach.class);

    private String pid;

    private VirtualMachine targetVM;

    private volatile boolean attached;

    public RuntimeAttach(String pid) {
        this.pid = pid;
    }

    @Override
    public synchronized boolean attach(String agentpath, String property) {
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
                this.targetVM.loadAgent(agentpath, property);
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
                }
            }
        }
        return this.attached;
    }
}
