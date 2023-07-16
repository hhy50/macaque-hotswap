package com.hhy.server;

import com.hhy.server.command.CommandLine;
import com.hhy.server.config.ServerConfig;
import com.hhy.server.config.LoggerName;
import com.hhy.server.process.JavaProcessHolder;
import com.hhy.server.service.DefaultServiceFactoryImpl;
import com.hhy.server.service.MacaqueService;
import com.hhy.server.service.ServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
