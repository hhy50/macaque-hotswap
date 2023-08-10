package six.eared.macaque.plugin.idea.thread;

import java.util.concurrent.ExecutorService;

public class Executors {

    private static final ExecutorService EXECUTOR = java.util.concurrent.Executors.newSingleThreadExecutor();

    public static void submit(Runnable runnable) {
        EXECUTOR.submit(runnable);
    }

}
