package six.eared.macaque.preload;


import io.github.hhy50.linker.annotations.Runtime;

@Runtime
public interface PatchMethodInvoker {
    /**
     *
     * @param args
     * @return
     */

    Object invoke(Object... args);
}
