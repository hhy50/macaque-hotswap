package six.eared.macaque.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import six.eared.macaque.core.jps.JavaProcessHolder;
import six.eared.macaque.server.command.CommandLine;
import six.eared.macaque.server.common.Banner;
import six.eared.macaque.server.config.LoggerName;
import six.eared.macaque.server.config.ServerConfig;
import six.eared.macaque.server.service.DefaultServiceFactoryImpl;
import six.eared.macaque.server.service.MacaqueService;
import six.eared.macaque.server.service.ServiceFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Macaque启动类
 * --serverPort=2023 --agentpath=C:\Users\haiyang\IdeaProjects\macaque-hot-swap\macaque-agent\build\libs\macaque-agent.jar --server
 */
public class MacaqueBootStrap {

    /**
     * 定时任务线程池
     */
    public static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    /**
     * 控制台日志
     */
    private static final Logger consoleLog = LoggerFactory.getLogger(LoggerName.CONSOLE);

    /**
     * 服务工厂
     */
    private static final ServiceFactory serviceFactory = new DefaultServiceFactoryImpl();

    /**
     * 启动
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        Banner.print();

        // 解析启动参数
        CommandLine commandLine = new CommandLine(args);
        ServerConfig serverConfig = getConfigFromCommandLine(commandLine);

        //设置启动模式，是否是服务端模式
        boolean serverMode = commandLine.hasOption("--server");
        System.setProperty("serverMode", Boolean.toString(serverMode));
        consoleLog.info("start mode: {}, serverConfig: {}", serverMode ? "server" : "client", serverConfig);

        if (serverMode) {
            checkServerConfig(commandLine, serverConfig);

            executor.scheduleAtFixedRate(JavaProcessHolder::refresh,
                    1000, 3000, TimeUnit.MILLISECONDS);
        }

        MacaqueService macaqueService = serviceFactory.newService(serverMode, serverConfig);
        macaqueService.start();

        Runtime.getRuntime().addShutdownHook(new Thread(macaqueService::stop));
    }

    /**
     * 检查服务端配置
     *
     * @param commandLine  启动参数
     * @param serverConfig 服务端配置
     */
    private static void checkServerConfig(CommandLine commandLine, ServerConfig serverConfig) {
        if (!commandLine.hasOption("--serverPort")) {
            consoleLog.info("Server Mode, must config serverPort");
            System.exit(-1);
        }
    }

    /**
     * 从启动参数中解析配置
     *
     * @param commandLine 启动参数
     * @return 配置
     */
    private static ServerConfig getConfigFromCommandLine(CommandLine commandLine) {
        return commandLine.toObject(ServerConfig.class);
    }
}
