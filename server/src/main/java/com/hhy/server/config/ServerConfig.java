package com.hhy.server.config;

public class ServerConfig {

    private int serverPort = 2020;

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
}
