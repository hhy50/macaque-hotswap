package com.hhy.server.service;

import com.hhy.common.util.Pair;
import com.hhy.server.attach.AttachFactory;
import com.hhy.server.attach.DefaultAttachFactory;
import com.hhy.server.command.DefaultCommandExecutor;
import com.hhy.server.config.LoggerName;
import com.hhy.server.config.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.hhy.server.process.JavaProcessHolder.getJavaProcess;

class MacaqueConsole implements MacaqueService {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.CONSOLE);

    private ServerConfig serverConfig;

    private AttachFactory attachFactory = DefaultAttachFactory.getInstance();

    private static List<String> ATTACH_HISTORY = new CopyOnWriteArrayList<>();

    public MacaqueConsole(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    @Override
    public void start() {
        log.info("console staring...");

        String pid = waitConsoleNextInput(true);
        boolean attach = this.attachFactory.createRuntimeAttach(serverConfig)
                .attach(pid);
        if (!attach) {
            log.error("attach error");
            System.exit(-1);
            return;
        }

        DefaultCommandExecutor defaultCommandExecutor = new DefaultCommandExecutor(pid);
        while (true) {
            String command = waitConsoleNextInput(false);
            defaultCommandExecutor.exec(command);
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

    public String waitConsoleNextInput(boolean isPid) {
        if (isPid) {
            printProcessList();
        }
        String input = null;
        loop: while (true) {
            try {
                Scanner scanner = new Scanner(System.in);
                input = scanner.nextLine();
                if (isPid) {
                    for (Pair<String, String> javaProcess : getJavaProcess()) {
                        if (javaProcess.getFirst().equals(input)) {
                            break loop;
                        }
                    }
                    log.info("pid: {}, is invalid process id", input);
                } else {
                    return input;
                }

            } catch (Exception e) {

            }
        }
        return input;
    }

    @Override
    public void stop() {

    }
}
