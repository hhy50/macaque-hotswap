package six.eared.macaque.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import six.eared.macaque.common.util.FileUtil;
import six.eared.macaque.common.util.Pair;
import six.eared.macaque.server.attach.Attach;
import six.eared.macaque.server.attach.AttachFactory;
import six.eared.macaque.server.attach.DefaultAttachFactory;
import six.eared.macaque.server.command.DefaultCommandExecutor;
import six.eared.macaque.server.config.LoggerName;
import six.eared.macaque.server.config.ServerConfig;
import six.eared.macaque.server.process.JavaProcessHolder;

import java.io.InputStream;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

class MacaqueConsole implements MacaqueService {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.CONSOLE);

    private ServerConfig serverConfig;

    private AttachFactory attachFactory = DefaultAttachFactory.getInstance();

    private Attach attach = null;

    private static List<String> ATTACH_HISTORY = new CopyOnWriteArrayList<>();

    public MacaqueConsole(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    @Override
    public void start() {
        log.info("console staring...");
        printBanner();

        String pid = waitConsoleNextInput(true);
        this.attach = this.attachFactory.createRuntimeAttach(serverConfig);
        if (!this.attach.attach(pid)) {
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

    private void printBanner() {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("macaque .txt")) {
            byte[] bytes = FileUtil.is2bytes(is);
            if (bytes != null) {
                log.info(new String(bytes));
            }
        } catch (Exception e) {

        }
    }

    private void printProcessList() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================== JPS ==========================").append("\n");
        for (Pair<String, String> process : JavaProcessHolder.getJavaProcess()) {
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
        loop:
        while (true) {
            try {
                Scanner scanner = new Scanner(System.in);
                input = scanner.nextLine();
                if (isPid) {
                    for (Pair<String, String> javaProcess : JavaProcessHolder.getJavaProcess()) {
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
