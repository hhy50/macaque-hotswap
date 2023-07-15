package com.hhy.server;

import com.hhy.server.commend.CommendLine;
import com.hhy.server.config.ServerConfig;
import com.hhy.server.console.Console;
import com.hhy.server.log.LoggerName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * --serverPort=2023 --agentPort=30312 --agentpath=aaaa --server
 */
public class MacaqueBootStrap {

    private static final Logger consoleLog = LoggerFactory.getLogger(LoggerName.CONSOLE);

    public static void main(String[] args) {
        CommendLine commendLine = new CommendLine(args);
        ServerConfig serverConfig = getConfigFromCommendLine(commendLine);

        boolean serverMode = commendLine.hasOption("--server");
        consoleLog.info("start mode: {}, serverConfig: {}", serverMode ? "server" : "client", serverConfig);

        System.setProperty("serverModel", Boolean.toString(serverMode));
        if (serverMode) {
            checkServerConfig(commendLine, serverConfig);
            startServer(serverConfig);
        }

        if (!serverMode){
            // finally startConsole
            startConsole(serverConfig);
        }
    }

    private static void startServer(ServerConfig serverConfig) {
        try {

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void checkServerConfig(CommendLine commendLine, ServerConfig serverConfig) {
        if (!commendLine.hasOption("--serverPort")) {
            consoleLog.info("Server Mode, must config serverPort");
            System.exit(-1);
        }
    }

    private static void startConsole(ServerConfig serverConfig) {
        Console console = new Console(serverConfig);
        console.start();
    }

    private static ServerConfig getConfigFromCommendLine(CommendLine commendLine) {
        return commendLine.toObject(ServerConfig.class);
    }
}
