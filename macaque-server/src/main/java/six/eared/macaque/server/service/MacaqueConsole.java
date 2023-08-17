package six.eared.macaque.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import six.eared.macaque.client.attach.Attach;
import six.eared.macaque.client.attach.DefaultAttachFactory;
import six.eared.macaque.client.common.PortNumberGenerator;
import six.eared.macaque.client.process.JavaProcessHolder;
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

    private final DefaultAttachFactory defaultAttachFactory;

    private Attach attach = null;

    public MacaqueConsole(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        this.defaultAttachFactory = new DefaultAttachFactory();
    }

    /**
     * 启动服务
     */
    @Override
    public void start() {
        console.info("console staring...");
        //获取pid
        String pid = waitConsoleNextInput(true);
        this.attach = this.defaultAttachFactory.createRuntimeAttach(pid);

        Integer agentPort = PortNumberGenerator.getPort(Integer.parseInt(pid));
        String property = String.format("port=%s,debug=%s", agentPort, Boolean.toString(this.serverConfig.isDebug()));
        if (!this.attach.attach(this.serverConfig.getAgentpath(), property)) {
            System.exit(-1);
            return;
        }
        console.info("attach success, pid={}", pid);
        DefaultCommandExecutor defaultCommandExecutor = new DefaultCommandExecutor(pid);
        while (true) {
            //获取命令，执行命令
            String command = waitConsoleNextInput(false);
            defaultCommandExecutor.exec(command);
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
