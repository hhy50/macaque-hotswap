package six.eared.macaque.server.command;

public interface CommandExecutor {

    /**
     * @param command
     */
    public void exec(String command);
}
