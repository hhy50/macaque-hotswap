package six.eared.macaque.agent.asm2.enhance;

import java.util.concurrent.atomic.AtomicInteger;

public class SimpleClassNameGenerator implements ClassNameGenerator {

    private static final AtomicInteger COUNTER = new AtomicInteger(1);

    @Override
    public String generate(String className, String methodName) {
        return className + "$Macaque$" + methodName+"$"+COUNTER.getAndIncrement();
    }

    @Override
    public String generateInnerAccessorName(String className)  {
        return className + "$Macaque$Accessor";
    }
}
