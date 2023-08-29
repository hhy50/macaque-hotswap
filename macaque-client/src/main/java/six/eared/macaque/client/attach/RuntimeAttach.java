package six.eared.macaque.client.attach;

import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import six.eared.macaque.client.common.AttachResultCode;

import java.io.IOException;


class RuntimeAttach implements Attach {

    private static final Logger log = LoggerFactory.getLogger(RuntimeAttach.class);

    private Integer pid;

    private VirtualMachine targetVM;


    public RuntimeAttach(Integer pid) {
        this.pid = pid;
    }

    @Override
    public synchronized int attach(String agentpath, String property) {
        int result = AttachResultCode.ERROR;
        VirtualMachineDescriptor virtualMachineDescriptor = null;
        for (VirtualMachineDescriptor descriptor : VirtualMachine.list()) {
            if (descriptor.id().equals(String.valueOf(pid))) {
                virtualMachineDescriptor = descriptor;
                break;
            }
        }

        try {
            if (virtualMachineDescriptor == null) {
                this.targetVM = VirtualMachine.attach(String.valueOf(pid));
            } else {
                this.targetVM = VirtualMachine.attach(virtualMachineDescriptor);
            }

            if (this.targetVM != null) {
                loadAgent(this.targetVM, agentpath, property);
                result = AttachResultCode.SUCCESS;
            }
        } catch (Exception e) {
            log.error("attach error", e);
        } finally {
            if (targetVM == null) {
                result = AttachResultCode.PROCESS_NOT_EXIST;
            }
            if (this.targetVM != null) {
                try {
                    targetVM.detach();
                } catch (IOException e) {
                }
            }
        }
        return result;
    }

    public void loadAgent(VirtualMachine virtualMachine, String agentpath, String property) throws Exception {
        try {
            virtualMachine.loadAgent(agentpath, property);
        } catch (Exception e) {
            if (e instanceof AgentLoadException) {
                if (!e.getMessage().equals("0")) {
                    throw e;
                }
            }
        }
    }
}
