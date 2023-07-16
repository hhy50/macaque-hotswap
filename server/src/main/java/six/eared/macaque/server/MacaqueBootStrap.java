package six.eared.macaque.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import six.eared.macaque.server.command.CommandLine;
import six.eared.macaque.server.config.LoggerName;
import six.eared.macaque.server.config.ServerConfig;
import six.eared.macaque.server.process.JavaProcessHolder;
import six.eared.macaque.server.service.DefaultServiceFactoryImpl;
import six.eared.macaque.server.service.MacaqueService;
import six.eared.macaque.server.service.ServiceFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * --serverPort=2023 --agentPort=30312 --agentpath=aaaa --server
 */
public class MacaqueBootStrap {

    public static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private static final Logger consoleLog = LoggerFactory.getLogger(LoggerName.CONSOLE);

    private static final ServiceFactory serviceFactory = DefaultServiceFactoryImpl.getINSTANCE();

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(args);
        ServerConfig serverConfig = getConfigFromCommandLine(commandLine);

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

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            macaqueService.stop();
        }));
    }

    private static void checkServerConfig(CommandLine commandLine, ServerConfig serverConfig) {
        if (!commandLine.hasOption("--serverPort")) {
            consoleLog.info("Server Mode, must config serverPort");
            System.exit(-1);
        }
    }

    private static ServerConfig getConfigFromCommandLine(CommandLine commandLine) {
        return commandLine.toObject(ServerConfig.class);
    }
}
