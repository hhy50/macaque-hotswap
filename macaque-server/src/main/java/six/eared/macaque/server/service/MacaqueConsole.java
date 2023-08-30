package six.eared.macaque.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import six.eared.macaque.core.client.MacaqueClient;
import six.eared.macaque.core.common.AttachResultCode;
import six.eared.macaque.core.jps.JavaProcessHolder;
import six.eared.macaque.common.util.Pair;
import six.eared.macaque.server.command.DefaultCommandExecutor;
import six.eared.macaque.server.config.LoggerName;
import six.eared.macaque.server.config.ServerConfig;

import java.util.Scanner;

/**
 * Macaque服务-控制台
 */
class MacaqueConsole implements MacaqueService {

    private static final Logger console = LoggerFactory.getLogger(LoggerName.CONSOLE);

    private ServerConfig serverConfig;

    public MacaqueConsole(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    /**
     * 启动服务
     */
    @Override
    public void start() {
        console.info("console staring...");
        Integer pid = Integer.valueOf(waitConsoleNextInput(true));

        MacaqueClient client = new MacaqueClient(pid);
        client.setAgentPath(serverConfig.getAgentpath());

        try {
            int attach = client.attach();
            if (attach == AttachResultCode.SUCCESS) {
                console.info("attach success, pid={}", pid);
                DefaultCommandExecutor defaultCommandExecutor = new DefaultCommandExecutor(client);
                while (true) {
                    //获取命令，执行命令
                    String command = waitConsoleNextInput(false);
                    defaultCommandExecutor.exec(command);
                }
            } else {
                throw new RuntimeException(String.format("attach fail, code = %d", attach));
            }
        } catch (Exception e) {
            console.error("attach fail", e);
            System.exit(-1);
        }
    }

    /**
     * 打印进程列表
     */
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

    /**
     * 从控制台输入获取命令
     *
     * @param isPid 是否是指定pid的命令
     * @return 输入内容
     */
    public String waitConsoleNextInput(boolean isPid) {
        if (isPid) {
            //打印进程列表
            printProcessList();
        }
        String input;
        loop:
        while (true) {
            try {
                Scanner scanner = new Scanner(System.in);
                input = scanner.nextLine();
                if (isPid) {
                    //检查输入的pid是否合法
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
                console.error("waitConsoleNextInput error", e);
            }
        }
        return input;
    }

    /**
     * 停止服务
     */
    @Override
    public void stop() {

    }
}
