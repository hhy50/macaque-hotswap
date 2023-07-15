package com.hhy.server.command;

import com.hhy.common.mbean.MBean;
import com.hhy.common.mbean.MBeanObjectName;
import com.hhy.common.rmi.ClassHotSwapRmiData;
import com.hhy.common.rmi.RmiResult;
import com.hhy.common.util.FileUtil;
import com.hhy.server.config.LoggerName;
import com.hhy.server.jmx.JmxClient;
import com.hhy.server.jmx.JmxConnectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultCommandExecutor implements CommandExecutor {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.auto());

    private final String pid;

    private JmxConnectPool jmxConnectPool = JmxConnectPool.getINSTANCE();

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

        JmxClient jmxClient = jmxConnectPool.getResource(pid);
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
