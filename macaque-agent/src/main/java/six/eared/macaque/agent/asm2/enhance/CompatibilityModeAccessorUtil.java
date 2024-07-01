package six.eared.macaque.agent.asm2.enhance;

import six.eared.macaque.agent.asm2.AsmField;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.ClassBuilder;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.agent.exceptions.AccessorCreateException;
import six.eared.macaque.asm.MethodVisitor;
import six.eared.macaque.asm.Opcodes;
import six.eared.macaque.asm.Type;
import six.eared.macaque.common.util.ClassUtil;
import six.eared.macaque.common.util.CollectionUtil;
import six.eared.macaque.common.util.StringUtil;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static six.eared.macaque.agent.asm2.AsmUtil.areturn;
import static six.eared.macaque.agent.asm2.AsmUtil.calculateLvbOffset;

public class CompatibilityModeAccessorUtil {

    private static final Map<String, ClazzDefinition> LOADED = new HashMap<>();

    /**
     * @param className          外部类类名
     * @param classNameGenerator 类名生成器
     * @param deepth             深度
     * @return
     */
    public static ClazzDefinition createAccessor(String className, ClassNameGenerator classNameGenerator, int deepth) {
        if (LOADED.containsKey(className)) {
            return LOADED.get(className);
        }

        String accessorName = classNameGenerator.generateInnerAccessorName(className);
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
            ClassBuilder classBuilder = generateAccessorClass(accessorName, outClazzDefinition, superAccessorName);

            collectAccessibleMethods(outClazzDefinition, classBuilder, superAccessor, classNameGenerator);
            collectAccessibleFields(outClazzDefinition, classBuilder, superAccessor);
            CompatibilityModeClassLoader.loadClass(classBuilder.getClassName(), classBuilder.toByteArray());

            ClazzDefinition accessorDefinition = AsmUtil.readClass(classBuilder.toByteArray());
            LOADED.put(className, accessorDefinition);
            return accessorDefinition;
        } catch (Exception e) {
            throw new AccessorCreateException(e);
        }
    }


    /**
     * @param accessorName
     * @param definition
     * @param superAccessorName
     * @return
     */
    private static ClassBuilder generateAccessorClass(String accessorName, ClazzDefinition definition,
                                                      String superAccessorName) {
        String outClassDesc = "L" + ClassUtil.simpleClassName2path(definition.getClassName()) + ";";
        String accessorDesc = ClassUtil.simpleClassName2path(accessorName);
        ClassBuilder classBuilder = AsmUtil
                .defineClass(Opcodes.ACC_PUBLIC, accessorName, superAccessorName, null, null)
                .defineConstruct(Opcodes.ACC_PUBLIC, new String[]{definition.getClassName()}, null, null)
                .accept(visitor -> {
                    visitor.visitMaxs(2, 2);
                    boolean containSupper = superAccessorName != null;

                    if (!containSupper) {
                        // this$0 = {outClassObj}
                        visitor.visitVarInsn(Opcodes.ALOAD, 0);
                        visitor.visitVarInsn(Opcodes.ALOAD, 1);
                        visitor.visitFieldInsn(Opcodes.PUTFIELD, accessorDesc, "this$0", "Ljava/lang/Object;");
                    }

                    // super({outClassObj}.this)
                    visitor.visitVarInsn(Opcodes.ALOAD, 0);
                    if (containSupper) {
                        visitor.visitVarInsn(Opcodes.ALOAD, 1);
                    }
                    visitor.visitMethodInsn(Opcodes.INVOKESPECIAL,
                            containSupper ? ClassUtil.simpleClassName2path(superAccessorName) : "java/lang/Object", "<init>",
                            containSupper ? AsmUtil.methodDesc("V", AsmUtil.toTypeDesc(definition.getSuperClassName())) : "()V", false);

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
                    visitor.visitFieldInsn(Opcodes.PUTSTATIC, accessorDesc, "LOOKUP", "Ljava/lang/invoke/MethodHandles$Lookup;");
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
        if (LOADED.containsKey(className)) {
            return LOADED.get(className).getClassName();
        }
        return null;
    }

    private static void collectAccessibleMethods(ClazzDefinition definition, ClassBuilder accessorClassBuilder, ClazzDefinition superAccessor,
                                                 ClassNameGenerator classNameGenerator) {
        try {
            String this0holder = findThis0Holder(accessorClassBuilder.getClassName(), superAccessor);
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

                // 不是继承而来的 或者 继承来的但是没有父accessor, 就生成方法调用
                accessorClassBuilder
                        .defineMethod(Opcodes.ACC_PUBLIC, method.getMethodName(), method.getDesc(), method.getExceptions(), method.getMethodSign())
                        .accept(visitor -> {
                            // invokerVirtual
                            invokerVirtual(visitor, accessorClassBuilder.getClassName(), this0holder, definition.getClassName(), method);
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
                    if (!accessibleSuperMethods.containsKey(superMethod.getUniqueDesc())) {
                        accessibleSuperMethods.put(superMethod.getUniqueDesc(), superMethod);
                    }
                }
                if (superAccessor != null) {
                    break;
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
                for (AsmMethod privateMethod : privateMethods) {
                    accessorClassBuilder
                            .defineMethod(Opcodes.ACC_PUBLIC, privateMethod.getMethodName(), privateMethod.getDesc(), privateMethod.getExceptions(), privateMethod.getMethodSign())
                            .accept(visitor -> {
                                // invokeSpecial
                                invokeSpecial(visitor, accessorClassBuilder.getClassName(), this0holder, definition.getClassName(),
                                        privateMethod);
                            });
                }
            }

            if (accessibleSuperMethods.size() > 0) {
                for (Map.Entry<String, AsmMethod> methodEntry : accessibleSuperMethods.entrySet()) {
                    AsmMethod superMethod = methodEntry.getValue();
                    accessorClassBuilder
                            .defineMethod(Opcodes.ACC_PUBLIC, "super_" + superMethod.getMethodName(), superMethod.getDesc(), superMethod.getExceptions(), superMethod.getMethodSign())
                            .accept(visitor -> {
                                // TODO 多态调用
                                // invokeSpecial
                                invokeSpecial(visitor, accessorClassBuilder.getClassName(),
                                        this0holder, definition.getClassName(), superMethod);
                            });
                }
            }
        } catch (Exception e) {
            throw new AccessorCreateException(e);
        }
    }

    /**
     * <p>
     *
     * </p>
     */
    private static void invokerVirtual(MethodVisitor visitor, String className, String this0holder, String ouClassName,
                                       AsmMethod asmMethod) {
        Type methodType = Type.getMethodType(asmMethod.getDesc());
        Type[] argumentTypes = methodType.getArgumentTypes();
        int lvbOffset = calculateLvbOffset(false, argumentTypes);
        visitor.visitMaxs(lvbOffset + 2, lvbOffset);

        visitor.visitVarInsn(Opcodes.ALOAD, 0);
        visitor.visitFieldInsn(Opcodes.GETFIELD, ClassUtil.simpleClassName2path(this0holder), "this$0", "Ljava/lang/Object;");
        visitor.visitTypeInsn(Opcodes.CHECKCAST, ClassUtil.simpleClassName2path(ouClassName));

        loadArgs(visitor, argumentTypes);
        visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, ClassUtil.simpleClassName2path(ouClassName), asmMethod.getMethodName(), methodType.getDescriptor(), false);

        // areturn
        areturn(visitor, methodType.getReturnType());
    }

    /**
     * <p>
     *      MethodType type = MethodType.methodType({arg0...n});
     *      MethodHandle mh = {@param accessorClassName}.LOOKUP.findSpecial({super_outClass}.class, "{@param methodName}", type, {@param superClassName}.class)
     *                              .bindTo({@param this0holder}.this$0);
     *      return mh.invoke({arg0...n});
     * </p>
     */
    /**
     * @param visitor             方法字节码的visitor
     * @param accessorClassName   访问器的类名
     * @param this0holder         持有this$0对象的类
     * @param outClassName        调用类的类名
     * @param beInvokedClass      被调用的类名
     * @param beInvokedMethod     被调用的方法
     * @param beInvokedMethodType 方法类型
     */
    private static void invokeSpecial(MethodVisitor visitor, String accessorClassName, String this0holder, String outClassName,
                                      AsmMethod asmMethod) {
        Type methodType = Type.getMethodType(asmMethod.getDesc());
        Type[] argumentTypes = methodType.getArgumentTypes();

        // locals = this(1) + args + type + mh
        int lvbOffset = calculateLvbOffset(asmMethod.isStatic(), argumentTypes);
        visitor.visitMaxs(lvbOffset + 4, lvbOffset + 2);

        adaptType(visitor, methodType.getReturnType());
        for (int i = 0; i < argumentTypes.length; i++) {
            if (i == 0) {
                adaptType(visitor, argumentTypes[i]);
                continue;
            }
            if (i == 1) {
                visitor.visitIntInsn(Opcodes.BIPUSH, argumentTypes.length - 1);
                visitor.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Class");
            } else {
                visitor.visitInsn(Opcodes.AASTORE);
            }
            visitor.visitInsn(Opcodes.DUP);
            if (i - 1 > 5) {
                visitor.visitIntInsn(Opcodes.BIPUSH, i - 1);
            } else {
                visitor.visitInsn(Opcodes.ICONST_0 + i - 1);
            }
            adaptType(visitor, argumentTypes[i]);
            if (i == argumentTypes.length - 1) {
                visitor.visitInsn(Opcodes.AASTORE);
            }
        }

        // MethodType type = MethodType.methodType({arg0...n});
        visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/invoke/MethodType", "methodType", selectMethodCallDesc(argumentTypes.length), false);
        visitor.visitVarInsn(Opcodes.ASTORE, lvbOffset + 0); // slot_0 = MethodType type

        // MethodHandle mh = {@param accessorClassName}.LOOKUP.findSpecial({super_outClass}.class, "{@param methodName}", type, {@param superClassName}.class)
        visitor.visitFieldInsn(Opcodes.GETSTATIC, ClassUtil.simpleClassName2path(accessorClassName), "LOOKUP", "Ljava/lang/invoke/MethodHandles$Lookup;");
        visitor.visitLdcInsn(Type.getType(AsmUtil.toTypeDesc(asmMethod.getClassName())));
        visitor.visitLdcInsn(asmMethod.getMethodName());
        visitor.visitVarInsn(Opcodes.ALOAD, lvbOffset + 0); // slot_0 = type
        visitor.visitLdcInsn(Type.getType(AsmUtil.toTypeDesc(outClassName)));
        visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/invoke/MethodHandles$Lookup", "findSpecial", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;", false);

        // .bindTo({@param this0holder}.this$0);
        visitor.visitVarInsn(Opcodes.ALOAD, 0);
        visitor.visitFieldInsn(Opcodes.GETFIELD, ClassUtil.simpleClassName2path(this0holder), "this$0", "Ljava/lang/Object;");
        visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "bindTo", "(Ljava/lang/Object;)Ljava/lang/invoke/MethodHandle;", false);
        visitor.visitVarInsn(Opcodes.ASTORE, lvbOffset + 1); // slot_1 = MethodHandle mh

        // return mh.invoke({arg0...n});
        visitor.visitVarInsn(Opcodes.ALOAD, lvbOffset + 1); // slot_1 = mh

        loadArgs(visitor, argumentTypes);
        visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "invoke", methodType.getDescriptor(), false);

        // return;
        areturn(visitor, methodType.getReturnType());
    }


    /**
     * 加载方法参数
     *
     * @param visitor
     * @param argumentTypes
     */
    private static void loadArgs(MethodVisitor visitor, Type[] argumentTypes) {
        int i = 0;
        for (Type argumentType : argumentTypes) {
            visitor.visitVarInsn(argumentType.getOpcode(Opcodes.ILOAD), i + 1);
            if (argumentType.getSort() == Type.DOUBLE || argumentType.getSort() == Type.LONG) i++;
            i++;
        }
    }

    /**
     * @param visitor
     * @param methodType
     */
    private static void adaptType(MethodVisitor visitor, Type methodType) {
        if (methodType.getReturnType().getSort() <= Type.DOUBLE) {
            visitor.visitFieldInsn(Opcodes.GETSTATIC, getPrimitiveClass(methodType.getReturnType().getClassName()), "TYPE", "Ljava/lang/Class;");
        } else {
            visitor.visitLdcInsn(methodType.getReturnType());
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

    /**
     * @param superClass
     * @param methodName
     * @param methodDesc
     * @return 返回这个方法是否继承而来的方法
     */
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
        switch (className) {
            case "void":
                return "java/lang/Void";
            case "boolean":
                return "java/lang/Boolean";
            case "char":
                return "java/lang/Character";
            case "byte":
                return "java/lang/Byte";
            case "short":
                return "java/lang/Short";
            case "int":
                return "java/lang/Integer";
            case "float":
                return "java/lang/Float";
            case "long":
                return "java/lang/Long";
            case "double":
                return "java/lang/Double";
            default:
                return "";
        }
    }
}
