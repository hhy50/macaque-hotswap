package six.eared.macaque.server.config;

public class LoggerName {

    public static String FILE = "File";

    public static String CONSOLE = "Console";

    public static String auto() {
        return Boolean.parseBoolean(System.getProperty("serverMode", "true")) ? FILE : CONSOLE;
    }
}
