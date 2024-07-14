package six.eared.macaque.agent.javassist;


import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.NotFoundException;
import six.eared.macaque.agent.asm2.ClassBuilder;

public class JavaSsistUtil {

    public static final ClassPool POOL = ClassPool.getDefault();

    static {
        POOL.importPackage("java.lang.invoke");
        POOL.importPackage("java.lang.reflect");
        POOL.importPackage("six.eared.macaque.agent.accessor.util");
    }

    public static ClassBuilder defineClass(int modifier, String className, String superClass, String[] interfaces)
            throws NotFoundException, CannotCompileException {
        return new ClassBuilder(modifier, className, superClass, interfaces);
    }
}
