package six.eared.macaque.server.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import six.eared.macaque.common.util.FileUtil;
import six.eared.macaque.mbean.MBean;
import six.eared.macaque.mbean.MBeanObjectName;
import six.eared.macaque.mbean.rmi.ClassHotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;
import six.eared.macaque.server.config.LoggerName;
import six.eared.macaque.server.jmx.JmxClient;
import six.eared.macaque.server.jmx.JmxClientResource;

public class DefaultCommandExecutor implements CommandExecutor {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.auto());

    private final String pid;

    private JmxClientResource jmxClientResource = JmxClientResource.getInstance();

    public DefaultCommandExecutor(String pid) {
        this.pid = pid;
    }

    @Override
    public void exec(String commandString) {
        String[] command = commandString.split("\\s+");

        if (command.length == 1) {
            if (command[0].equals("quit")) {
                System.exit(-1);
            }
        }

        if (command.length != 2) {
            log.error("usage: {className} {newClassFilepath}");
            return;
        }

        JmxClient jmxClient = jmxClientResource.getResource(pid);
        if (jmxClient != null) {
            MBean mBean = jmxClient.getMBean(MBeanObjectName.HOT_SWAP_MBEAN);
            RmiResult result = mBean.process(new ClassHotSwapRmiData(command[0],
                    FileUtil.readBytes(command[1])));
            if (result.isSuccess()) {
                log.info(result.getMessage());
            } else {
                log.error("error: {}", result.getMessage());
            }
        }
    }
}
