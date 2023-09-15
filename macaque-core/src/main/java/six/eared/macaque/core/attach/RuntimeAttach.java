package six.eared.macaque.core.attach;

import com.sun.tools.attach.*;
import six.eared.macaque.core.common.AttachResultCode;

import java.io.IOException;


class RuntimeAttach implements Attach {


    private Integer pid;

    private VirtualMachine targetVM;


    public RuntimeAttach(Integer pid) {
        this.pid = pid;
    }

    @Override
    public synchronized int attach(String agentpath, String property)
            throws IOException, AttachNotSupportedException, AgentLoadException, AgentInitializationException {
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
        } finally {
            if (this.targetVM == null) {
                result = AttachResultCode.PROCESS_NOT_EXIST;
            }
            if (this.targetVM != null) {
                this.targetVM.detach();
            }
        }
        return result;
    }

    public void loadAgent(VirtualMachine virtualMachine, String agentpath, String property)
            throws AgentLoadException, AgentInitializationException, IOException {
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
