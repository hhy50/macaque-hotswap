package six.eared.macaque.client.attach;


import java.util.HashMap;
import java.util.Map;

public class DefaultAttachFactory implements AttachFactory {

    private static final Map<String, RuntimeAttach> HISTORY = new HashMap<>();

    @Override
    public synchronized Attach createRuntimeAttach(String pid) {
        RuntimeAttach attach = HISTORY.get(pid);
        if (attach != null) {
            return attach;
        }
        RuntimeAttach runtimeAttach = new RuntimeAttach(pid);
        HISTORY.put(pid, runtimeAttach);
        return runtimeAttach;
    }
}
