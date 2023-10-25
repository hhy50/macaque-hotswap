package six.eared.macaque.agent.asm2.enhance;

import six.eared.macaque.agent.asm2.*;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.asm2.classes.MethodVisitorProxy;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.agent.exceptions.AccessorCreateException;
import six.eared.macaque.asm.ClassVisitor;
import six.eared.macaque.asm.MethodVisitor;
import six.eared.macaque.asm.Opcodes;
import six.eared.macaque.asm.Type;
import six.eared.macaque.common.util.ClassUtil;
import six.eared.macaque.common.util.CollectionUtil;
import six.eared.macaque.common.util.StringUtil;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CompatibilityModeAccessorUtil {

    /**
     * @param className          外部类类名
     * @param classNameGenerator 类名生成器
     * @param deepth             深度
     * @return
     */
    public static ClazzDefinition createAccessor(String className, ClassNameGenerator classNameGenerator, int deepth) {
        String innerAccessorName = classNameGenerator.generateInnerAccessorName(className);
        if (CompatibilityModeClassLoader.isLoaded(innerAccessorName)) {
            return null;
        }
        try {
            ClazzDefinition outClazzDefinition = AsmUtil.readOriginClass(className);
            String superClassName = outClazzDefinition.getSuperClassName();
            ClazzDefinition superAccessor = null;
            if (deepth > 0) {
                if (StringUtil.isNotEmpty(superClassName)
                        && !isSystemClass(superClassName)) {
                    superAccessor = createAccessor(superClassName, classNameGenerator, --deepth);
                }
            }
            String superAccessorName = tryGetAccessorClassName(superClassName, classNameGenerator);
            ClassBuilder classBuilder = generateAccessorClass(innerAccessorName, outClazzDefinition, superAccessorName);

            collectAccessibleMethods(outClazzDefinition, classBuilder, superAccessor, classNameGenerator);
            collectAccessibleFields(outClazzDefinition, classBuilder, superAccessor);

            CompatibilityModeClassLoader.loadClass(classBuilder.getClassName(), classBuilder.toByteArray());
            return AsmUtil.readClass(classBuilder.toByteArray());
        } catch (Exception e) {
            throw new AccessorCreateException(e);
        }
    }


    /**
     * @param innerAccessorName
     * @param definition
     * @param superAccessorName
     * @return
     */
    private static ClassBuilder generateAccessorClass(String innerAccessorName, ClazzDefinition definition,
                                                      String superAccessorName) {
        String outClassDesc = "L" + ClassUtil.simpleClassName2path(definition.getClassName()) + ";";
        String innerAccessorDesc = ClassUtil.simpleClassName2path(innerAccessorName);
        ClassBuilder classBuilder = AsmUtil
                .defineClass(Opcodes.ACC_PUBLIC, innerAccessorName, superAccessorName, null, null)
                .defineConstruct(Opcodes.ACC_PUBLIC, new String[]{definition.getClassName()}, null, null)
                .accept(visitor -> {
                    visitor.visitMaxs(2, 2);
                    boolean containSupper = superAccessorName != null;

                    if (!containSupper) {
                        // this$0 = {outClassObj}
                        visitor.visitVarInsn(Opcodes.ALOAD, 0);
                        visitor.visitVarInsn(Opcodes.ALOAD, 1);
                        visitor.visitFieldInsn(Opcodes.PUTFIELD, innerAccessorDesc, "this$0", "Ljava/lang/Object;");
                    }

                    // super({outClassObj}.this)
                    visitor.visitVarInsn(Opcodes.ALOAD, 0);
                    if (containSupper) {
                        visitor.visitVarInsn(Opcodes.ALOAD, 1);
                    }
                    visitor.visitMethodInsn(Opcodes.INVOKESPECIAL,
                            containSupper ? ClassUtil.simpleClassName2path(superAccessorName) : "java/lang/Object", "<init>",
                            containSupper ? AsmUtil.methodType("V", AsmUtil.toTypeDesc(definition.getSuperClassName())) : "()V", false);

                    visitor.visitInsn(Opcodes.RETURN);
                    visitor.visitEnd();
                });

        /**
         * public {Accessor_Class} this$0;
         */
        if (superAccessorName == null) {
            classBuilder.defineField(Opcodes.ACC_PUBLIC, "this$0", "Ljava/lang/Object;", null, null);
        }

        /**
         * public static MethodHandles.Lookup LOOKUP;
         */
        classBuilder.defineField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "LOOKUP", "Ljava/lang/invoke/MethodHandles$Lookup;",
                null, null);

        /**
         *         static {
         *             Constructor<?> constructor = MethodHandles.Lookup.class.getDeclaredConstructors()[0];
         *             constructor.setAccessible(true);
         *             LOOKUP = (MethodHandles.Lookup) constructor.newInstance(EarlyClass.class);
         *         }
         */
        classBuilder.defineMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "<clinit>", "()V", null, null)
                .accept(visitor -> {
                    visitor.visitMaxs(5, 2);

                    // Constructor<?> constructor = MethodHandles.Lookup.class.getDeclaredConstructors()[0];
                    visitor.visitLdcInsn(Type.getType("Ljava/lang/invoke/MethodHandles$Lookup;"));
                    visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getDeclaredConstructors",
                            "()[Ljava/lang/reflect/Constructor;", false);
                    visitor.visitInsn(Opcodes.ICONST_0);
                    visitor.visitInsn(Opcodes.AALOAD);
                    visitor.visitVarInsn(Opcodes.ASTORE, 0);

                    // constructor.setAccessible(true);
                    visitor.visitVarInsn(Opcodes.ALOAD, 0);
                    visitor.visitInsn(Opcodes.ICONST_1);
                    visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/reflect/Constructor", "setAccessible", "(Z)V", false);

                    // LOOKUP = (MethodHandles.Lookup) constructor.newInstance(EarlyClass.class);
                    visitor.visitVarInsn(Opcodes.ALOAD, 0);
                    visitor.visitInsn(Opcodes.ICONST_1);
                    visitor.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
                    visitor.visitInsn(Opcodes.DUP);
                    visitor.visitInsn(Opcodes.ICONST_0);
                    visitor.visitLdcInsn(Type.getType(outClassDesc));
                    visitor.visitInsn(Opcodes.AASTORE);
                    visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/reflect/Constructor", "newInstance",
                            "([Ljava/lang/Object;)Ljava/lang/Object;", false);
                    visitor.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/invoke/MethodHandles$Lookup");
                    visitor.visitFieldInsn(Opcodes.PUTSTATIC, innerAccessorDesc, "LOOKUP", "Ljava/lang/invoke/MethodHandles$Lookup;");
                    visitor.visitInsn(Opcodes.RETURN);
                });
        return classBuilder;
    }

    /**
     * @param className
     * @param classNameGenerator
     * @return
     */
    private static String tryGetAccessorClassName(String className, ClassNameGenerator classNameGenerator) {
        String accessorName = classNameGenerator.generateInnerAccessorName(className);
        if (CompatibilityModeClassLoader.isLoaded(accessorName)) {
            return accessorName;
        }
        return null;
    }

    private static void collectAccessibleMethods(ClazzDefinition definition, ClassBuilder accessorClassBuilder, ClazzDefinition superAccessor,
                                                 ClassNameGenerator classNameGenerator) {
        try {
            String this0holder = findThis0Holder(accessorClassBuilder.getClassName(), superAccessor);
            String outClassDesc = "L" + ClassUtil.simpleClassName2path(definition.getClassName()) + ";";

            Set<AsmMethod> privateMethods = new HashSet<>();
            Set<AsmMethod> superMethods = new HashSet<>();

            // my all method
            for (AsmMethod method : definition.getAsmMethods()) {
                if (method.isConstructor() || method.isClinit()) {
                    continue;
                }

                // 私有方法
                if (method.isPrivate()) {
                    privateMethods.add(method);
                    continue;
                }

                // 继承而来 （如果自己重写了父类的方法, 就保存父类的字节码，防止 super调用）
                boolean inherited = inherited(definition.getSuperClassName(), method.getMethodName(), method.getDesc());
                if (inherited && superAccessor != null) {
                    continue;
                }
                // 不是继承而来的 或者 继承来的但是没有父accessor
                accessorClassBuilder
                        .defineMethod(Opcodes.ACC_PUBLIC, method.getMethodName(), method.getDesc(), method.getExceptions(), method.getMethodSign())
                        .accept(visitor -> {
                            visitor.visitVarInsn(Opcodes.ALOAD, 0);
                            visitor.visitFieldInsn(Opcodes.GETFIELD,
                                    ClassUtil.simpleClassName2path(this0holder), "this$0", "Ljava/lang/Object;");
                            visitor.visitTypeInsn(Opcodes.CHECKCAST,
                                    ClassUtil.simpleClassName2path(definition.getClassName()));
                            visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                    ClassUtil.simpleClassName2path(definition.getClassName()), method.getMethodName(), method.getDesc(), false);
                            visitor.visitInsn(Opcodes.ARETURN);
                            visitor.visitMaxs(1, 1);
                        });
            }

            // non private method in super class
            ClazzDefinition superClassDefinition = null;
            String superClassName = definition.getSuperClassName();
            if (StringUtil.isNotEmpty(superClassName)) {
                superClassDefinition = AsmUtil.readOriginClass(superClassName);

                for (AsmMethod superMethod : superClassDefinition.getAsmMethods()) {
                    if (superMethod.getMethodName().equals("<init>") || superMethod.getMethodName().equals("<clinit>")) {
                        continue;
                    }
                    // skip private
                    if ((superMethod.getModifier() & Opcodes.ACC_PRIVATE) > 0) {
                        continue;
                    }
                    superMethods.add(superMethod);
                }
            }

            // default method with interface class
            if (Environment.getJdkVersion() > 7) {

            }

            // 将私有方法绑定到另一个类，方便以后修改
            if (CollectionUtil.isNotEmpty(privateMethods)) {
                for (AsmMethod privateMethod : privateMethods) {
                    String bindMethodName = privateMethod.getMethodName();
                    String bindClassName = classNameGenerator.generate(definition.getClassName(), bindMethodName);
                    MethodBindInfo methodBindInfo = new MethodBindInfo();
                    methodBindInfo.setBindClass(bindClassName);
                    methodBindInfo.setBindMethod(bindMethodName);
                    methodBindInfo.setPrivateMethod(true);
                    privateMethod.setMethodBindInfo(methodBindInfo);
                }
            }

            superMethods.clear();
            if (CollectionUtil.isNotEmpty(superMethods)) {
                Map<String, AsmMethod> superMethodMap = superMethods.stream().collect(Collectors
                        .toMap(item -> item.getMethodName() + item.getDesc(), Function.identity()));
                superClassDefinition = superClassDefinition == null ? AsmUtil.readOriginClass(superClassName) : superClassDefinition;
                while (superClassDefinition != null && !superMethodMap.isEmpty()) {
                    superClassDefinition.revisit(new ClassVisitor(Opcodes.ASM5) {
                        @Override
                        public MethodVisitor visitMethod(int access, String methodName, String methodDesc, String signature, String[] exceptions) {
                            if (superMethods.stream()
                                    .anyMatch(item -> item.getMethodName().equals(methodName) && item.getDesc().equals(methodDesc))) {
                                MethodBuilder methodBuilder = accessorClassBuilder
                                        .defineMethod(Opcodes.ACC_PUBLIC, "super_" + methodName, methodDesc, exceptions, signature);
                                superMethodMap.remove(methodName + methodDesc);
                                return new MethodVisitorProxy(methodBuilder.getMethodVisitor());
                            }
                            return null;
                        }
                    });
                    superClassDefinition = AsmUtil.readOriginClass(superClassDefinition.getSuperClassName());
                }
            }
        } catch (Exception e) {
            throw new AccessorCreateException(e);
        }
    }

    /**
     * 获取this0字段在哪个类里面
     * @param superAccessor
     * @return
     */
    private static String findThis0Holder(String accessorName, ClazzDefinition superAccessor) throws ClassNotFoundException, NoSuchFieldException {
        if (superAccessor == null) {
            return accessorName;
        }
        if (superAccessor.getSuperClassName() == null || superAccessor.getSuperClassName().equals("java.lang.Object")) {
            return superAccessor.getClassName();
        }

        Class<?> clazz = Class.forName(superAccessor.getClassName());
        Field this$0 = clazz.getField("this$0");
        return this$0.getDeclaringClass().getName();
    }

    private static void collectAccessibleFields(ClazzDefinition definition, ClassBuilder containSuper, ClazzDefinition classBuilder) {
        // my all field
        for (AsmField asmField : definition.getAsmFields()) {
            if ((asmField.getModifier() & Opcodes.ACC_PRIVATE) > 0) {

            }
        }
        // non private field in super class

    }

    private static boolean inherited(String superClass, String methodName, String methodDesc)
            throws ClassNotFoundException, IOException {
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
