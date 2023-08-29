package six.eared.macaque.server.command;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import six.eared.macaque.client.c.MacaqueClient;
import six.eared.macaque.common.type.FileType;
import six.eared.macaque.common.util.FileUtil;
import six.eared.macaque.mbean.rmi.ClassHotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;
import six.eared.macaque.server.config.LoggerName;

public class DefaultCommandExecutor implements CommandExecutor {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.CONSOLE);

    private final MacaqueClient client;

    public DefaultCommandExecutor(MacaqueClient client) {
        this.client = client;
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
        try {
            RmiResult result = client.hotswap(new ClassHotSwapRmiData(FileType.Class.getType(), FileUtil.readBytes(classPath)));
            log.info("exec result: [{}]", result.getData());
        } catch (Exception e) {
            log.error("hotswap error", e);
        }
    }
}
