package com.hhy.server.config;

public class ServerConfig {

    private int serverPort = 2020;

    private String agentpath;

    public String getAgentpath() {
        return agentpath;
    }

    public void setAgentpath(String agentpath) {
        this.agentpath = agentpath;
    }
}
