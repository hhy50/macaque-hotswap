package six.eared.macaque.agent.enhance;

import java.util.concurrent.atomic.AtomicInteger;

public class BindMethodNameGenerator {

    private static final AtomicInteger COUNTER = new AtomicInteger(1);

    public String generate(String className, String methodName) {
        return className + "$Macaque$" + methodName + "$" + COUNTER.getAndIncrement();
    }
}
