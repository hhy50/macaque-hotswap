package six.eared.macaque.library.patch;

import six.eared.macaque.preload.PatchedInvocation;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

public class ClassloaderEnhancer implements ClassFileTransformer {

    private static Set<Class<?>> CLASSES = new HashSet<>();

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        return new byte[0];
    }

    public static Class<?> loadClass(PatchedInvocation invocation) {
        Object[] args = invocation.getArgs();
        String className = (String) args[0];
        try {
            Class<?> clazz = (Class<?>) invocation.invoke();
            if (clazz == null && (
                    className.startsWith("six.eared.macaque") || className.startsWith("io.github.hhy50.linker")
            )) {

            }
            return clazz;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
