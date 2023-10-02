package six.eared.macaque.agent.asm2.enhance;

import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.ClassBuilder;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.agent.exceptions.AccessorCreateException;
import six.eared.macaque.asm.Opcodes;
import six.eared.macaque.common.util.ClassUtil;
import six.eared.macaque.common.util.StringUtil;

import java.util.HashSet;
import java.util.Set;

public class CompatibilityModeAccessorUtil {

    public static ClazzDefinition createAccessor(ClazzDefinition definition, ClassNameGenerator classNameGenerator, int deepth) {
        ClazzDefinition superAccessor = null;
        String superClassName = definition.getSuperClassName();
        if (deepth > 0) {
            if (StringUtil.isNotEmpty(superClassName)
                    && !isSystemClass(superClassName)) {
                try {
                    superAccessor = createAccessor(AsmUtil.readOriginClass(superClassName), classNameGenerator, --deepth);
                } catch (ClassNotFoundException e) {
                    if (Environment.isDebug()) {
                        e.printStackTrace();
                    }
                }
            }
        }
        String superAccessorName = tryGetAccessorClassName(superClassName, classNameGenerator);
        ClassBuilder classBuilder = generateAccessorClass(definition, superAccessorName, classNameGenerator);

        collectAccessibleMethods(definition, classBuilder, superAccessor);
        collectAccessibleFields(definition, superAccessorName == null, classBuilder);

        CompatibilityModeClassLoader.loadClass(classBuilder.getClassName(), classBuilder.toByteArray());
        return AsmUtil.readClass(classBuilder.toByteArray());
    }


    /**
     * @param definition
     * @param superAccessorName
     * @param classNameGenerator
     * @return
     */
    private static ClassBuilder generateAccessorClass(ClazzDefinition definition,
                                                      String superAccessorName, ClassNameGenerator classNameGenerator) {

        String innerAccessorName = classNameGenerator.generateInnerAccessorName(definition.getClassName());
        String superClassDesc = "L" + ClassUtil.simpleClassName2path(definition.getClassName()) + ";";

        return AsmUtil
                .defineClass(Opcodes.ACC_PUBLIC, innerAccessorName, superAccessorName, null, null)
                .defineField(Opcodes.ACC_SYNTHETIC | Opcodes.ACC_FINAL, "this$0", superClassDesc, null, null)
                .defineConstruct(Opcodes.ACC_PUBLIC, new String[]{definition.getClassName()}, null, null)
                .accept(visitor -> {
                    visitor.visitVarInsn(Opcodes.ALOAD, 0);
                    visitor.visitVarInsn(Opcodes.ALOAD, 1);
                    visitor.visitFieldInsn(Opcodes.PUTFIELD, ClassUtil.simpleClassName2path(innerAccessorName), "this$0", superClassDesc);
                    visitor.visitVarInsn(Opcodes.ALOAD, 0);
                    visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
                    visitor.visitInsn(Opcodes.RETURN);
                    visitor.visitEnd();
                });
    }

    /**
     * @param className
     * @param classNameGenerator
     * @return
     */
    private static String tryGetAccessorClassName(String className, ClassNameGenerator classNameGenerator) {
        String accessorName = classNameGenerator.generateInnerAccessorName(className);
//        Set<Class<?>> loadedClass = InstrumentationUtil.findLoadedClass(Environment.getInst(), accessorName);
//        if (CollectionUtil.isNotEmpty(loadedClass)) {
//            return accessorName;
//        }
        if (CompatibilityModeClassLoader.isLoaded(accessorName)) {
            return accessorName;
        }
        return null;
    }

    private static void collectAccessibleMethods(ClazzDefinition definition, ClassBuilder accessorClassBuilder, ClazzDefinition superAccessor) {
        try {
            String innerAccessorName = accessorClassBuilder.getClassName();
            String outClassDesc = "L" + ClassUtil.simpleClassName2path(definition.getClassName()) + ";";

            Set<AsmMethod> privateMethods = new HashSet<>();
            Set<AsmMethod> superMethods = new HashSet<>();

            // my all method
            for (AsmMethod method : definition.getAsmMethods()) {
                if (method.getMethodName().equals("<init>") || method.getMethodName().equals("<clinit>")) {
                    continue;
                }

                // 私有方法
                if ((method.getModifier() & Opcodes.ACC_PRIVATE) > 0) {
                    privateMethods.add(method);
                    continue;
                }

                // 继承而来 （如果自己重写了父类的方法, 就保存父类的字节码，防止 super调用）
                if (superAccessor != null
                        && inherited(definition.getSuperClassName(), method.getMethodName(), method.getDesc())) {
                    superMethods.add(method);
                    continue;
                }

                // 没有重写的
                accessorClassBuilder
                        .defineMethod(Opcodes.ACC_PUBLIC, method.getMethodName(), method.getDesc(), method.getExceptions(), method.getMethodSign())
                        .accept(visitor -> {
                            visitor.visitVarInsn(Opcodes.ALOAD, 0);
                            visitor.visitFieldInsn(Opcodes.GETFIELD,
                                    ClassUtil.simpleClassName2path(innerAccessorName), "this$0", outClassDesc);
                            visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                    ClassUtil.simpleClassName2path(definition.getClassName()), method.getMethodName(), method.getDesc(), false);
                            visitor.visitInsn(Opcodes.ARETURN);
                            visitor.visitMaxs(1, 1);
                        });
            }

            // non private method in super class
            if (superAccessor == null && StringUtil.isNotEmpty(definition.getSuperClassName())) {
                String superClassName = definition.getSuperClassName();
                ClazzDefinition superClassDefinition = null;

                superClassDefinition = AsmUtil.readOriginClass(superClassName);

                for (AsmMethod superMethod : superClassDefinition.getAsmMethods()) {
                    if (superMethod.getMethodName().equals("<init>") || superMethod.getMethodName().equals("<clinit>")) {
                        continue;
                    }
                    // skip private
                    if ((superMethod.getModifier() & Opcodes.ACC_PRIVATE) > 0) {
                        continue;
                    }

                    if (superClassName.equals("java.lang.Object")) {
                        continue;
                    }
                    accessorClassBuilder
                            .defineMethod(Opcodes.ACC_PUBLIC, superMethod.getMethodName(), superMethod.getDesc(), superMethod.getExceptions(), superMethod.getMethodSign())
                            .accept(visitor -> {
                                visitor.visitVarInsn(Opcodes.ALOAD, 0);
                                visitor.visitFieldInsn(Opcodes.GETFIELD,
                                        ClassUtil.simpleClassName2path(innerAccessorName), "this$0", outClassDesc);
                                visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                        ClassUtil.simpleClassName2path(definition.getClassName()), superMethod.getMethodName(), superMethod.getDesc(), false);
                                visitor.visitInsn(Opcodes.ARETURN);
                                visitor.visitMaxs(1, 1);
                            });
                }
            }

            // default method with interface class

            System.out.println();
        } catch (Exception e) {
            throw new AccessorCreateException(e);
        }
    }

    private static boolean inherited(String superClass, String methodName, String methodDesc) throws ClassNotFoundException {
        while (StringUtil.isNotEmpty(superClass)
                && !superClass.equals("java.lang.Object")) {
            ClazzDefinition definition = AsmUtil.readOriginClass(superClass);
            if (definition.hasMethod(methodName, methodDesc)) {
                return true;
            }
            superClass = definition.getSuperClassName();
        }
        return false;
    }

    private static void collectAccessibleFields(ClazzDefinition definition, boolean containSuper, ClassBuilder classBuilder) {
        // need rewrite

        // my all field

        // non private field in super class
    }


    public static boolean isSystemClass(String className) {
        if (className.startsWith("java.") || className.startsWith("javax.") || className.startsWith("sun.")) {
            return true;
        }
        if (className.contains(".internal.") || className.contains(".reflect.") || className.contains(".lang.")
                || className.contains(".io.") || className.contains(".net.")) {
            return true;
        }
        if (className.contains("java$") || className.contains("javax$") || className.contains("sun$")) {
            return true;
        }
        return false;
    }
}
