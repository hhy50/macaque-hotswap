package six.eared.macaque.server.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import six.eared.macaque.client.jmx.JmxClient;
import six.eared.macaque.client.jmx.JmxClientResourceManager;
import six.eared.macaque.common.type.FileType;
import six.eared.macaque.common.util.FileUtil;
import six.eared.macaque.mbean.MBean;
import six.eared.macaque.mbean.MBeanObjectName;
import six.eared.macaque.mbean.rmi.ClassHotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;
import six.eared.macaque.server.config.LoggerName;

public class DefaultCommandExecutor implements CommandExecutor {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.CONSOLE);

    private final String pid;

    public DefaultCommandExecutor(String pid) {
        this.pid = pid;
    }

    @Override
    public void exec(String commandString) {
        String[] command = commandString.split("\\s+");

        if (command.length != 1) {
            log.error("usage: {newClassFilepath}");
            return;
        }

        if (command[0].equals("quit")) {
            System.exit(-1);
        }

        String classPath = command[0];
        JmxClient jmxClient = JmxClientResourceManager.getInstance().getResource(pid);
        if (jmxClient != null) {
            MBean<ClassHotSwapRmiData> mBean = jmxClient.getMBean(MBeanObjectName.HOT_SWAP_MBEAN);
            RmiResult result = mBean.process(new ClassHotSwapRmiData(
                    FileType.Class.getType(),
                    FileUtil.readBytes(classPath))
            );
            if (result.isSuccess()) {
                log.info(result.getMessage());
            } else {
                log.error("error: {}", result.getMessage());
            }
        }
    }
}
