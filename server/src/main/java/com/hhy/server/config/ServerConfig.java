package com.hhy.server.config;

public class ServerConfig {

    private int serverPort = 2020;

    private String agentpath;

    private int agentPort = 3030;

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

    @Override
    public String toString() {
        return "ServerConfig{" +
                "serverPort=" + serverPort +
                ", agentpath='" + agentpath + '\'' +
                ", agentPort=" + agentPort +
                '}';
    }
}
