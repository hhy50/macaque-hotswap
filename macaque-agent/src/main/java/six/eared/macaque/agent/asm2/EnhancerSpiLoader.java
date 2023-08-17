package six.eared.macaque.agent.asm2;

import java.util.ServiceLoader;

public class EnhancerSpiLoader {

    public void load() {
        ServiceLoader<Enhancer> enhancers = ServiceLoader.load(Enhancer.class);
        for (Enhancer enhancer : enhancers) {

        }
    }
}
