package com.hhy.server.config;

public class ServerConfig {

    private int serverPort = 2020;

    private String agentpath;

    private int agentPort = 3030;

    private String logfilePath;

    public String getAgentpath() {
        return agentpath;
    }

    public void setAgentpath(String agentpath) {
        this.agentpath = agentpath;
    }

    public int getAgentPort() {
        return agentPort;
    }

    public void setAgentPort(int agentPort) {
        this.agentPort = agentPort;
    }

    public String getAgentProperties() {
        return String.format("port=%s", agentPort);
    }

    public String getLogfilePath() {
        return logfilePath;
    }

    public void setLogfilePath(String logfilePath) {
        this.logfilePath = logfilePath;
    }
}
