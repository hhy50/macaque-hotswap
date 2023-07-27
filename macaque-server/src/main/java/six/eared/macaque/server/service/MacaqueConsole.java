package six.eared.macaque.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import six.eared.macaque.common.util.FileUtil;
import six.eared.macaque.common.util.Pair;
import six.eared.macaque.server.attach.Attach;
import six.eared.macaque.server.attach.DefaultAttachFactory;
import six.eared.macaque.server.command.DefaultCommandExecutor;
import six.eared.macaque.server.config.LoggerName;
import six.eared.macaque.server.config.ServerConfig;
import six.eared.macaque.server.process.JavaProcessHolder;

import java.io.InputStream;
import java.util.Scanner;

class MacaqueConsole implements MacaqueService {

    private static final Logger console = LoggerFactory.getLogger(LoggerName.CONSOLE);

    private ServerConfig serverConfig;

    private DefaultAttachFactory defaultAttachFactory;

    private Attach attach = null;

    public MacaqueConsole(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        this.defaultAttachFactory = new DefaultAttachFactory(serverConfig);
    }

    @Override
    public void start() {
        console.info("console staring...");
        printBanner();

        String pid = waitConsoleNextInput(true);
        this.attach = this.defaultAttachFactory.createRuntimeAttach(pid);
        if (!this.attach.attach()) {
            System.exit(-1);
            return;
        }
        console.info("attach success, pid={}", pid);
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
                console.info(new String(bytes));
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
        console.info(sb.toString());
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
                    console.info("pid: {}, is invalid process id", input);
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
