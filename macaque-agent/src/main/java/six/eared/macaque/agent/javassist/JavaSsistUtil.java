package six.eared.macaque.agent.javassist;


import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.NotFoundException;

public class JavaSsistUtil {

    public static final ClassPool POOL;

    static {
        POOL = new ClassPool(ClassPool.getDefault()) {
//            @Override
//            public CtClass get(String classname) throws NotFoundException {
//                CtClass ctClass = super.get(classname);
//                if (ctClass == null) {
//                    Set<Class<?>> loadedClass = InstrumentationUtil.findLoadedClass(Environment.getInst(), classname);
//                    if (CollectionUtil.isNotEmpty(loadedClass)) {
//                        Class<?> clazz = loadedClass.iterator().next();
//                    }
//                }
//                return ctClass;
//            }
            //            @Override
//            public Class toClass(CtClass ct, ClassLoader loader) throws CannotCompileException {
//                String className = ct.getName();
//                Set<Class<?>> loadedClass = InstrumentationUtil.findLoadedClass(Environment.getInst(), className);
//                if (CollectionUtil.isNotEmpty(loadedClass)) {
//                    Class<?> clazz = loadedClass.iterator().next();
//                    loader = clazz.getClassLoader();
//                }
//                return toClass(ct, null, loader, null);
//            }
        };
        POOL.importPackage("java.lang.invoke");
        POOL.importPackage("java.lang.reflect");
        POOL.importPackage("six.eared.macaque.agent.accessor.util");
    }

    public static JavassistClassBuilder defineClass(int modifier, String className, String superClass, String[] interfaces)
            throws NotFoundException, CannotCompileException {
        return new JavassistClassBuilder(modifier, className, superClass, interfaces);
    }


}
