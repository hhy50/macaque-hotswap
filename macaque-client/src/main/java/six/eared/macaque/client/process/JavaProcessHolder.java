package six.eared.macaque.client.process;

import com.sun.tools.attach.VirtualMachine;
import six.eared.macaque.client.common.PID;
import six.eared.macaque.common.util.Pair;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class JavaProcessHolder {

    private volatile static List<Pair<String /** pid */,
                                    String /** processName */>> PROCESS_LIST = new CopyOnWriteArrayList<>();

    static {
        refresh();
    }

    public static List<Pair<String, String>> getJavaProcess() {
        return PROCESS_LIST;
    }

    public static void refresh() {
        PROCESS_LIST = VirtualMachine.list().stream()
                .map(item -> {
                    // TODO 有些Java进程不显示进程名称, 比如 idea
                    return Pair.of(item.id(), item.displayName());
                }).filter(item -> {
                    return !item.getFirst().equals(PID.getCurrentPid().toString());
                }).collect(Collectors.toList());
    }
}
