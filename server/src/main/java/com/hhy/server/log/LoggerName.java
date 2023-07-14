package com.hhy.server.log;

public class LoggerName {

    public static boolean serverMode = true;

    public static String FILE = "File";

    public static String CONSOLE = "Console";

    public static String auto() {
        return serverMode ? FILE : CONSOLE;
    }
}
