package six.eared.macaque.agent.asm2.enhance;

import six.eared.macaque.agent.asm2.*;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.agent.exceptions.AccessorCreateException;
import six.eared.macaque.asm.Opcodes;
import six.eared.macaque.asm.Type;
import six.eared.macaque.common.util.ClassUtil;
import six.eared.macaque.common.util.CollectionUtil;
import six.eared.macaque.common.util.ReflectUtil;
import six.eared.macaque.common.util.StringUtil;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
            classBuilder.defineField(Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, "this$0", "Ljava/lang/Object;", null, null);
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
            Map<String, AsmMethod> accessibleSuperMethods = new HashMap<>();

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
                            visitor.visitFieldInsn(Opcodes.GETFIELD, ClassUtil.simpleClassName2path(this0holder), "this$0", "Ljava/lang/Object;");
                            visitor.visitTypeInsn(Opcodes.CHECKCAST, ClassUtil.simpleClassName2path(definition.getClassName()));
                            visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, ClassUtil.simpleClassName2path(definition.getClassName()), method.getMethodName(), method.getDesc(), false);
                            visitor.visitInsn(Opcodes.ARETURN);
                            visitor.visitMaxs(1, 1);
                        });
            }

            // non private method in super class
            String superClassName = definition.getSuperClassName();
            ClazzDefinition superClassDefinition = AsmUtil.readOriginClass(superClassName);
            while (superClassDefinition != null) {
                Set<AsmMethod> items = new HashSet<>();
                for (AsmMethod superMethod : superClassDefinition.getAsmMethods()) {
                    if (superMethod.isConstructor() || superMethod.isClinit()) {
                        continue;
                    }
                    // skip private
                    if (superMethod.isPrivate()) {
                        continue;
                    }
                    if (!accessibleSuperMethods.containsKey(superMethod.getMethodName() + "|" + superMethod.getDesc())) {
                        accessibleSuperMethods.put(superMethod.getMethodName() + "|" + superMethod.getDesc(), superMethod);
                    }
                }
                if (superClassDefinition.getClassName().equals("java.lang.Object") || superClassDefinition.getSuperClassName() == null) {
                    break;
                }
                superClassDefinition = AsmUtil.readOriginClass(superClassDefinition.getSuperClassName());
            }

            // default method in interface class
            if (Environment.getJdkVersion() > 7) {

            }

            // 对于私有方法的访问方式有两种：
            // 1. MethodHandler的方式（可维护性强）
            // 2. 将字节码绑定到新的类（性能好）
            if (CollectionUtil.isNotEmpty(privateMethods)) {

            }

            /**
             *             MethodType type = MethodType.methodType(String.class);
             *             MethodHandle mh = lookup
             *                     .findSpecial({super_outClass}.class, "test1", type, {outClass}.class)
             *                     .bindTo({outClass}.this);
             *             mh.invoke({arg0...n});
             */
            if (accessibleSuperMethods.size() > 0) {
                for (Map.Entry<String, AsmMethod> methodEntry : accessibleSuperMethods.entrySet()) {
                    AsmMethod asmMethod = methodEntry.getValue();
                    accessorClassBuilder
                            .defineMethod(Opcodes.ACC_PUBLIC, "super_" + asmMethod.getMethodName(), asmMethod.getDesc(), asmMethod.getExceptions(), asmMethod.getMethodSign())
                            .accept(visitor -> {
                                Type methodType = Type.getMethodType(asmMethod.getDesc());
                                Type[] argumentTypes = methodType.getArgumentTypes();
                                int lvbOffset = argumentTypes.length;

                                visitor.visitMaxs(6, lvbOffset + 2);
                                if (methodType.getReturnType().getSort() <= Type.DOUBLE) {
                                    visitor.visitFieldInsn(Opcodes.GETSTATIC, getPrimitiveClass(methodType.getReturnType().getClassName()), "TYPE", "Ljava/lang/Class;");
                                } else {
                                    visitor.visitLdcInsn(methodType.getReturnType());
                                }
                                for (int i = 0; i < lvbOffset; i++) {
                                    if (i == 0) {
                                        if (argumentTypes[i].getSort() <= Type.DOUBLE) {
                                            visitor.visitFieldInsn(Opcodes.GETSTATIC, getPrimitiveClass(argumentTypes[i].getClassName()), "TYPE", "Ljava/lang/Class;");
                                        } else {
                                            visitor.visitLdcInsn(argumentTypes[i]);
                                        }
                                        continue;
                                    }
                                    if (i == 1) {
                                        visitor.visitInsn(Opcodes.ICONST_1);
                                        visitor.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Class");
                                        visitor.visitInsn(Opcodes.DUP);
                                        visitor.visitInsn(Opcodes.ICONST_0);
                                    }
                                    if (argumentTypes[i].getSort() <= Type.DOUBLE) {
                                        visitor.visitFieldInsn(Opcodes.GETSTATIC, getPrimitiveClass(argumentTypes[i].getClassName()), "TYPE", "Ljava/lang/Class;");
                                    } else {
                                        visitor.visitLdcInsn(argumentTypes[i]);
                                    }
                                    visitor.visitInsn(Opcodes.AASTORE);
                                    if (i == argumentTypes.length - 1) {
                                        visitor.visitInsn(Opcodes.AASTORE);
                                    }
                                }

                                // MethodType type = MethodType.methodType(String.class);
                                visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/invoke/MethodType", "methodType", selectMethodCallDesc(argumentTypes.length), false);
                                visitor.visitVarInsn(Opcodes.ASTORE, lvbOffset + 1); // slot_1 = MethodType type

                                // LOOKUP.findSpecial({super_outClass}.class, "{method}", type, {outClass}.class)
                                visitor.visitFieldInsn(Opcodes.GETSTATIC, ClassUtil.simpleClassName2path(accessorClassBuilder.getClassName()), "LOOKUP", "Ljava/lang/invoke/MethodHandles$Lookup;");
                                visitor.visitLdcInsn(Type.getType(AsmUtil.toTypeDesc(ClassUtil.simpleClassName2path(asmMethod.getClassName()))));
                                visitor.visitLdcInsn(asmMethod.getMethodName());
                                visitor.visitVarInsn(Opcodes.ALOAD, lvbOffset + 1); // slot_1 = type
                                visitor.visitLdcInsn(Type.getType(outClassDesc));
                                visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/invoke/MethodHandles$Lookup", "findSpecial", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;", false);

                                // .bindTo({outClass}.this);
                                visitor.visitVarInsn(Opcodes.ALOAD, 0);
                                visitor.visitFieldInsn(Opcodes.GETFIELD, this0holder, "this$0", "Ljava/lang/Object;");
                                visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "bindTo", "(Ljava/lang/Object;)Ljava/lang/invoke/MethodHandle;", false);
                                visitor.visitVarInsn(Opcodes.ASTORE, lvbOffset + 2); // slot_2 = MethodHandle mh

                                // mh.invoke({arg0...n});
                                visitor.visitVarInsn(Opcodes.ALOAD, lvbOffset + 2); // slot_2 = mh
                                for (int i = 0; i < argumentTypes.length; i++) {
                                    visitor.visitVarInsn(Opcodes.ALOAD, i + 1);
                                }
                                visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "invoke", asmMethod.getDesc(), false);

                                if (methodType.getReturnType().getSort() == Type.VOID) {
                                    visitor.visitInsn(Opcodes.RETURN);
                                } else {
                                    visitor.visitInsn(methodType.getOpcode(Opcodes.IRETURN));
                                }
                            });
                }
            }
        } catch (Exception e) {
            throw new AccessorCreateException(e);
        }
    }

    /**
     * 获取this0字段在哪个类里面
     *
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

    private static String selectMethodCallDesc(int ac) {
        if (ac == 0) {
            return "(Ljava/lang/Class;)Ljava/lang/invoke/MethodType;";
        }
        if (ac == 1) {
            return "(Ljava/lang/Class;Ljava/lang/Class;)Ljava/lang/invoke/MethodType;";
        }
        return "(Ljava/lang/Class;Ljava/lang/Class;[Ljava/lang/Class;)Ljava/lang/invoke/MethodType;";
    }

    private static String getPrimitiveClass(String className) {
        Class<?> wClass = (Class<?>) ReflectUtil.invokeStaticMethod(Class.class, "getPrimitiveClass", className);
        return ClassUtil.simpleClassName2path(wClass.getName());
    }
}
