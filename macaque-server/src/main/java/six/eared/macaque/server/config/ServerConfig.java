package six.eared.macaque.server.config;

public class ServerConfig {

    private String agentpath;

    private boolean debug;

    public String getAgentpath() {
        return agentpath;
    }

    public void setAgentpath(String agentpath) {
        this.agentpath = agentpath;
    }

    public Boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public String toString() {
        return "ServerConfig{" +
                "agentpath=" + agentpath +
                ", debug=" + debug +
                '}';
    }
}
