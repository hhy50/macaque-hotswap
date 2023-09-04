package six.eared.macaque.agent.asm2;

import java.util.Iterator;
import java.util.ServiceLoader;

public class EnhancerSpiLoader {

    public static Iterator<Enhancer> load() {
        ServiceLoader<Enhancer> enhancers = ServiceLoader.load(Enhancer.class);
        return enhancers.iterator();
    }
}
