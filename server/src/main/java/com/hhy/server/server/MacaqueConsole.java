package com.hhy.server.server;

import com.hhy.common.mbean.MBean;
import com.hhy.common.mbean.MBeanObjectName;
import com.hhy.common.rmi.ClassHotSwapRmiData;
import com.hhy.common.util.FileUtil;
import com.hhy.common.util.Pair;
import com.hhy.server.attach.AttachFactory;
import com.hhy.server.attach.DefaultAttachFactory;
import com.hhy.server.config.ServerConfig;
import com.hhy.server.jmx.JmxClient;
import com.hhy.server.log.LoggerName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Scanner;

import static com.hhy.server.process.JavaProcessHolder.getJavaProcess;

class MacaqueConsole implements MacaqueService {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.CONSOLE);

    private ServerConfig serverConfig;

    private AttachFactory attachFactory = DefaultAttachFactory.getInstance();

    public MacaqueConsole(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    @Override
    public void start() {
        log.info("console staring...");

        String pid = waitConsoleInputPid();
        boolean attach = this.attachFactory.createRuntimeAttach(serverConfig)
                .attach(pid);
        if (!attach) {
            System.exit(-1);
            return;
        }

        // TODO input comment and exec comment
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
        sb.append("========================== JPS ==========================").append("\n");
        for (Pair<String, String> process : getJavaProcess()) {
            String format = String.format("[%s] %s", process.getFirst(), process.getSecond());
            sb.append(format).append("\n");
        }
        sb.append("========================== JPS ==========================").append("\n");
        log.info(sb.toString());
    }

    public String waitConsoleInputPid() {
        printProcessList();

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

    @Override
    public void stop() {

    }
}
