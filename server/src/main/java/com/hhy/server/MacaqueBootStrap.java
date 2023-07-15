package com.hhy.server;

import com.hhy.server.commend.CommendLine;
import com.hhy.server.config.ServerConfig;
import com.hhy.server.log.LoggerName;
import com.hhy.server.process.JavaProcessHolder;
import com.hhy.server.server.DefaultServiceFactoryImpl;
import com.hhy.server.server.MacaqueService;
import com.hhy.server.server.ServiceFactory;
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
        CommendLine commendLine = new CommendLine(args);
        ServerConfig serverConfig = getConfigFromCommendLine(commendLine);

        boolean serverMode = commendLine.hasOption("--server");
        consoleLog.info("start mode: {}, serverConfig: {}", serverMode ? "server" : "client", serverConfig);

        System.setProperty("serverModel", Boolean.toString(serverMode));
        if (serverMode) {
            checkServerConfig(commendLine, serverConfig);

            executor.scheduleAtFixedRate(JavaProcessHolder::refresh,
                    1000, 3000, TimeUnit.MILLISECONDS);
        }

        MacaqueService macaqueService = serviceFactory.newService(serverMode, serverConfig);
        macaqueService.start();


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            macaqueService.stop();
        }));
    }

    private static void checkServerConfig(CommendLine commendLine, ServerConfig serverConfig) {
        if (!commendLine.hasOption("--serverPort")) {
            consoleLog.info("Server Mode, must config serverPort");
            System.exit(-1);
        }
    }

    private static ServerConfig getConfigFromCommendLine(CommendLine commendLine) {
        return commendLine.toObject(ServerConfig.class);
    }
}
