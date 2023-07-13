package com.hhy.server;

import com.hhy.common.mbean.MBean;
import com.hhy.common.mbean.MBeanObjectName;
import com.hhy.server.attach.RuntimeAttach;
import com.hhy.server.commend.CommendLine;
import com.hhy.server.config.ServerConfig;
import com.hhy.server.jmx.JmxClient;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.IOException;
import java.util.Scanner;


/**
 * --serverPort=2023 --agentPort=30312 --agentpath=aaaa --server
 */
public class ServerBootStrap {

    public static void main(String[] args) {
        CommendLine commendLine = new CommendLine(args);
        System.out.println(commendLine);

        ServerConfig serverConfig = getConfigFromCommendLine(commendLine);


        // finally startConsole
        if (!commendLine.hasOption("--server")){
            startConsole(serverConfig);
        }
    }

    private static void startConsole(ServerConfig serverConfig) {
        System.out.println("console start...");

        for (VirtualMachineDescriptor virtualMachineDescriptor : VirtualMachine.list()) {
            String format = String.format("[%s] %s", virtualMachineDescriptor.id(),
                    virtualMachineDescriptor.displayName());
            System.out.println(format);
        }
        String pid = waitConsoleInputPid();
        new RuntimeAttach(serverConfig).attach(pid);
        try {
            JmxClient jmxClient = new JmxClient("127.0.0.1", serverConfig.getAgentPort());
            jmxClient.connect();

            MBean mBean = jmxClient.getMBean(MBeanObjectName.HOT_SWAP_MBEAN);
            mBean.process(new String[] {"hello, i'm agent server"});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String waitConsoleInputPid() {
        return new Scanner(System.in).next();
    }

    private static ServerConfig getConfigFromCommendLine(CommendLine commendLine) {
        return commendLine.toObject(ServerConfig.class);
    }
}
