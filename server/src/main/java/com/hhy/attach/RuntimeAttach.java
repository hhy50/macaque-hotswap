package com.hhy.attach;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.util.List;

public class RuntimeAttach implements Attach {

    @Override
    public void attach(String pid) {
        List<VirtualMachineDescriptor> list = VirtualMachine.list();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
