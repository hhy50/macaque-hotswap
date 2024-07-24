package six.eared.macaque.agent.javassist;


import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.NotFoundException;

public class JavaSsistUtil {

    public static final ClassPool POOL;

    static {
        POOL = new ClassPool(ClassPool.getDefault());
        POOL.importPackage("java.lang.invoke");
        POOL.importPackage("java.lang.reflect");
        POOL.importPackage("six.eared.macaque.agent.accessor.util");
    }

    public static JavassistClassBuilder defineClass(int modifier, String className, String superClass, String[] interfaces)
            throws NotFoundException, CannotCompileException {
        return new JavassistClassBuilder(modifier, className, superClass, interfaces);
    }
}
