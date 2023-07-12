package com.hhy.server.test;

import com.hhy.common.mbean.MBean;
import com.hhy.common.mbean.MBeanObjectName;
import com.hhy.server.attach.RuntimeAttach;
import com.hhy.server.jmx.JmxClient;

import java.io.IOException;

public class TestAttachMain {
    public static void main(String[] args) throws IOException {
        int port = 3302;
        new RuntimeAttach().attach("12356", "port="+port);
        JmxClient jmxClient = new JmxClient("127.0.0.1", port);
        jmxClient.connect();
        MBean mBean = jmxClient.getMBean(MBeanObjectName.HOT_SWAP_MBEAN);

        mBean.process(new String[] {"hello, i'm agent server"});
    }
}
