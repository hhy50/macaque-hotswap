package com.hhy.server.console;

import com.hhy.common.mbean.MBean;
import com.hhy.common.mbean.MBeanObjectName;
import com.hhy.common.rmi.ClassHotSwapRmiData;
import com.hhy.common.util.FileUtil;
import com.hhy.common.util.Pair;
import com.hhy.server.attach.RuntimeAttach;
import com.hhy.server.config.ServerConfig;
import com.hhy.server.jmx.JmxClient;
import com.hhy.server.log.LoggerName;
import com.hhy.server.thrad.MacaqueThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Scanner;

import static com.hhy.server.process.JavaProcessHolder.getJavaProcess;

public class Console extends MacaqueThread {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.CONSOLE);

    private ServerConfig serverConfig;

    public Console(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public void run() {
        log.info("console staring...");
        printProcessList();
        String pid = waitConsoleInputPid();
        new RuntimeAttach(serverConfig).attach(pid);

        try {
            JmxClient jmxClient = new JmxClient("127.0.0.1", serverConfig.getAgentPort());
            jmxClient.connect();

            MBean mBean = jmxClient.getMBean(MBeanObjectName.HOT_SWAP_MBEAN);
            mBean.process(new ClassHotSwapRmiData("com.hhy.server.test.target.A",
                    FileUtil.readBytes("C:\\Users\\haiyang\\IdeaProjects\\six-eared-macaque\\server\\build\\classes\\java\\test\\com\\hhy\\server\\test\\target\\A.class")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void printProcessList() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================== Process ==========================").append("\n");
        for (Pair<String, String> process : getJavaProcess()) {
            String format = String.format("[%s] %s", process.getFirst(), process.getSecond());
            sb.append(format).append("\n");
        }
        sb.append("========================== Process ==========================").append("\n");
        sb.append("input pid:");
        log.info(sb.toString());
    }

    public String waitConsoleInputPid() {
        String pid = null;
        loop: while (true) {
            try {
                Scanner scanner = new Scanner(System.in);
                pid = scanner.next();
                for (Pair<String, String> javaProcess : getJavaProcess()) {
                    if (javaProcess.getFirst().equals(pid)) {
                        break loop;
                    }
                }
                log.info("pid: {}, is invalid process id", pid);
            } catch (Exception e) {

            }
        }
        return pid;
    }
}
