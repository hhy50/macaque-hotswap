package com.hhy.server.attach;

public interface Attach {

    /**
     * attach process
     * @param pid
     */
    public boolean attach(String pid);

    /**
     * detach process
     * @param pid
     * @return
     */
    public boolean detach(String pid);
}
