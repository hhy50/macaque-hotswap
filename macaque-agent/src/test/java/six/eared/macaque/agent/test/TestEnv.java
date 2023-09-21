package six.eared.macaque.agent.test;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import six.eared.macaque.common.jps.PID;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class TestEnv {
    static {
        URL resource = TestEnv.class.getClassLoader().getResource("macaque-agent-lightweight.jar");
        try {
            VirtualMachine attach = VirtualMachine.attach(String.valueOf(PID.getCurrentPid()));
            attach.loadAgent(new File(resource.toURI()).getPath(), "");
            attach.detach();
        } catch (AttachNotSupportedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (AgentLoadException e) {
            throw new RuntimeException(e);
        } catch (AgentInitializationException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
