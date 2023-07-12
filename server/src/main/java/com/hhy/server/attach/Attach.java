package com.hhy.server.attach;

public interface Attach {

    /**
     * attach process
     * @param pid
     */
    public void attach(String pid, String properties);
}
