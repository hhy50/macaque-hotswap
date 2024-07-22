package six.eared.macaque.agent.accessor;

import javassist.*;
import javassist.bytecode.*;
import lombok.SneakyThrows;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import six.eared.macaque.agent.asm2.AsmField;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.enhance.AccessorClassNameGenerator;
import six.eared.macaque.agent.enhance.CompatibilityModeClassLoader;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.agent.exceptions.AccessorCreateException;
import six.eared.macaque.agent.javassist.JavaSsistUtil;
import six.eared.macaque.agent.javassist.JavassistClassBuilder;
import six.eared.macaque.common.util.ClassUtil;
import six.eared.macaque.common.util.ReflectUtil;
import six.eared.macaque.common.util.StringUtil;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class CompatibilityModeAccessorUtilV2 {
    private static final AtomicInteger COUNTER = new AtomicInteger(1);

    private static final Map<String, Accessor> LOADED = new HashMap<>();

    /**
     * @param className          外部类类名
     * @param classNameGenerator 类名生成器
     * @param deepth             深度
     * @return
     */
    public static Accessor createAccessor(String className, AccessorClassNameGenerator classNameGenerator, int deepth) {
        if (LOADED.containsKey(className)) {
            return LOADED.get(className);
        }
        String accessorName = classNameGenerator.generate(className);
        try {
            ClazzDefinition clazzDefinition = AsmUtil.readOriginClass(className);
            String superClassName = clazzDefinition.getSuperClassName();
            Accessor superAccessor = null;
            if (deepth > 0) {
                if (StringUtil.isNotEmpty(superClassName)
                        && !isSystemClass(superClassName)) {
                    superAccessor = createAccessor(superClassName, classNameGenerator, --deepth);
                }
            }
            String superAccessorName = tryGetAccessorClassName(superClassName, classNameGenerator);
            JavassistClassBuilder javassistClassBuilder = generateAccessorClass(clazzDefinition.getClassName(), accessorName, superAccessorName);

            collectAccessibleMethods(clazzDefinition, javassistClassBuilder, superAccessor);
            collectAccessibleFields(clazzDefinition, javassistClassBuilder, superAccessor);
            CompatibilityModeClassLoader.loadClass(javassistClassBuilder.getClassName(), javassistClassBuilder.toByteArray());

            Accessor accessor = new Accessor(className, AsmUtil.readClass(javassistClassBuilder.toByteArray()), superAccessor);
            LOADED.put(className, accessor);
            return accessor;
        } catch (Exception e) {
            throw new AccessorCreateException(e);
        }
    }


    /**
     * @param className
     * @param accessorName
     * @param superAccessorName
     * @return
     */
    private static JavassistClassBuilder generateAccessorClass(String className, String accessorName, String superAccessorName) throws NotFoundException, CannotCompileException {
        boolean containSupper = superAccessorName != null;
        JavassistClassBuilder javassistClassBuilder = JavaSsistUtil
                .defineClass(Modifier.PUBLIC, accessorName, superAccessorName, null)
                .defineField("public static final MethodHandles$Lookup LOOKUP = Util.lookup(" + className + ".class);");
        if (!containSupper) {
            javassistClassBuilder.defineField(Modifier.PUBLIC | AccessFlag.SYNTHETIC, "this$0", "java.lang.Object");
        }
        javassistClassBuilder.defineConstructor(String.format("public %s(Object this$0) { %s }",
                ClassUtil.toSimpleName(accessorName), containSupper ? "super(this$0);" : "this.this$0=this$0;"));
        return javassistClassBuilder;
    }

    /**
     * @param className
     * @param classNameGenerator
     * @return
     */
    private static String tryGetAccessorClassName(String className, AccessorClassNameGenerator classNameGenerator) {
        if (LOADED.containsKey(className)) {
            return LOADED.get(className).getClassName();
        }
        return null;
    }

    private static void collectAccessibleMethods(ClazzDefinition definition, JavassistClassBuilder accessorBuilder, Accessor superAccessor) {
        try {
            // my all method
            for (AsmMethod method : definition.getAsmMethods()) {
                if (method.isConstructor() || method.isClinit()) {
                    continue;
                }
                if (method.isStatic()) {
                    invokerStatic(accessorBuilder, definition.getClassName(), method);
                } else if (method.isPrivate()) {
                    invokeSpecial(accessorBuilder, method, definition.getClassName(), definition.getClassName());
                } else {
                    // 继承而来 （如果自己重写了父类的方法, 就保存父类的字节码，防止 super调用）
                    boolean inherited = inherited(definition.getSuperClassName(), method.getMethodName(), method.getDesc());
                    if (inherited && superAccessor != null) {
                        continue;
                    }
                    // 不是继承而来的 或者 继承来的但是没有父accessor, 就生成方法调用
                    invokerVirtual(accessorBuilder, definition.getClassName(), method);
                }
            }

            // 收集父类中所有可以访问到的方法
            Set<String> unique = new HashSet<>();
            ClazzDefinition superClassDefinition = AsmUtil.readOriginClass(definition.getSuperClassName());
            while (superClassDefinition != null) {
                for (AsmMethod superMethod : superClassDefinition.getAsmMethods()) {
                    if (superMethod.isConstructor() || superMethod.isClinit() || superMethod.isPrivate()) {
                        continue;
                    }
                    String key = superMethod.getMethodName() + "#" + superMethod.getDesc();
                    if (!unique.contains(key)) {
                        unique.add(superMethod.getMethodName() + "#" + superMethod.getDesc());
                        invokeSpecial(accessorBuilder, superMethod, definition.getClassName(), superClassDefinition.getClassName());
                    }
                }
                if (superAccessor != null || superClassDefinition.getClassName().equals("java.lang.Object") || superClassDefinition.getSuperClassName() == null) {
                    break;
                }
                superClassDefinition = AsmUtil.readOriginClass(superClassDefinition.getSuperClassName());
            }
            // default method in interface class
            if (Environment.getJdkVersion() > 7) {

            }
        } catch (Exception e) {
            throw new AccessorCreateException(e);
        }
    }

    private static void collectAccessibleFields(ClazzDefinition definition, JavassistClassBuilder javassistClassBuilder, Accessor superAccessor) {
        try {
            // my all field
            for (AsmField asmField : definition.getAsmFields()) {
                getField(asmField, definition.getClassName(), javassistClassBuilder);
                if (!asmField.isFinal()) {
                    setField(asmField, definition.getClassName(), javassistClassBuilder);
                }
            }
            // non private field in super class


        } catch (Exception e) {
            throw new AccessorCreateException(e);
        }
    }

    private static void getField(AsmField asmField, String owner, JavassistClassBuilder javassistClassBuilder) throws CannotCompileException {
        Type fieldType = Type.getType(asmField.getDesc());
        String type = fieldType.getClassName();
        String name = asmField.getFieldName();
        String body = null;
        if (asmField.isPrivate()) {
            String unpacking = getUnpacking(fieldType);
            body = "Field field = " + owner + ".class.getDeclaredField(\"" + name + "\"); field.setAccessible(true);" +
                    "return ((" + type + ") Util." + unpacking + "(field.get(" + (asmField.isStatic() ? "null" : "this$0") + ")));";
        } else if (asmField.isStatic()) {
            body = "return " + owner + "." + name + ";";
        } else {
            body = "return ((" + owner + ") this$0)." + name + ";";
        }
        javassistClassBuilder.defineMethod(String.format("public " + (asmField.isStatic() ? "static " : "") + "%s " + Accessor.FIELD_GETTER_PREFIX + "%s() { %s }", type, name, body));
    }

    private static void setField(AsmField asmField, String owner, JavassistClassBuilder javassistClassBuilder) throws CannotCompileException {
        Type fieldType = Type.getType(asmField.getDesc());
        String type = fieldType.getClassName();
        String name = asmField.getFieldName();
        String body = null;
        if (asmField.isPrivate()) {
            body = "Field field = " + owner + ".class.getDeclaredField(\"" + name + "\"); field.setAccessible(true);" +
                    "field.set(" + (asmField.isStatic() ? "null" : "this$0") + ", Util.wrapping(arg));";
        } else if (asmField.isStatic()) {
            body = owner + "." + name + " = arg;";
        } else {
            body = "((" + owner + ") this$0)." + name + " = arg;";
        }
        javassistClassBuilder.defineMethod(String.format("public " + (asmField.isStatic() ? "static " : "") + "void " + Accessor.FIELD_SETTER_PREFIX + "%s(%s arg) { %s }", name, type, body));
    }

    private static void invokerVirtual(JavassistClassBuilder javassistClassBuilder, String this0Class,
                                       AsmMethod method) throws CannotCompileException {
        String methodName = method.getMethodName();
        Type methodType = Type.getMethodType(method.getDesc());
        Type[] args = methodType.getArgumentTypes();

        String rType = methodType.getReturnType().getClassName();
        String[] argVars = IntStream.range(0, args.length).mapToObj(i -> "var_" + i).toArray(String[]::new);

        String declare = String.format("public %s %s(%s)",
                rType, methodName, IntStream.range(0, args.length).mapToObj(i -> args[i].getClassName() + " " + argVars[i]).collect(Collectors.joining(",")));

        String body = (rType.equals("void") ? "" : "return (" + rType + ")") +
                "((" + this0Class + ") this$0)." + methodName + "(" + String.join(",", argVars) + ");";
        javassistClassBuilder.defineMethod(declare + "{" + body + "}");
    }

    private static void invokerStatic(JavassistClassBuilder javassistClassBuilder, String this0Class, AsmMethod method) throws CannotCompileException {
        String methodName = method.getMethodName();
        Type methodType = Type.getMethodType(method.getDesc());
        Type[] args = methodType.getArgumentTypes();

        String rType = methodType.getReturnType().getClassName();
        String[] argVars = IntStream.range(0, args.length).mapToObj(i -> "var_" + i).toArray(String[]::new);

        String declare = String.format("public static %s %s(%s)",
                rType, methodName, IntStream.range(0, args.length).mapToObj(i -> args[i].getClassName() + " " + argVars[i]).collect(Collectors.joining(",")));
        String body = null;
        if (method.isPrivate()) {
            String mhVar = methodName + "_MH_" + COUNTER.getAndIncrement();
            String argsTypeDeclare = Arrays.stream(args).map(type -> type.getClassName() + ".class").collect(Collectors.joining(","));
            javassistClassBuilder
                    .defineField("private static final MethodHandle " + mhVar + " = LOOKUP.findStatic(" + this0Class + ".class,\"" + methodName + "\", " +
                            "MethodType.methodType(" + rType + ".class,new Class[]{" + argsTypeDeclare + "}));")
                    .defineMethod(declare + "{ throw new RuntimeException(\"not impl\"); }", (bytecode) -> {
                        bytecode.addGetstatic(javassistClassBuilder.getClassName(), mhVar, "Ljava/lang/invoke/MethodHandle;");
                        loadArgs(bytecode, args);
                        bytecode.addInvokevirtual("java/lang/invoke/MethodHandle", "invoke", methodType.getDescriptor());
                        areturn(bytecode, methodType.getReturnType());

                        bytecode.setMaxLocals(2+args.length);
                        bytecode.setMaxStack(2+AsmUtil.calculateLvbOffset(true, args));
                    });
        } else {
            body = (rType.equals("void") ? "" : "return (" + rType + ")") +
                    this0Class + "." + methodName + "(" + String.join(",", argVars) + ");";
            javassistClassBuilder.defineMethod(declare + "{" + body + "}");
        }
    }

    /**
     * @param this0Class            this0Class
     * @param method                生成的方法
     * @param javassistClassBuilder 构造器
     */
    private static void invokeSpecial(JavassistClassBuilder javassistClassBuilder, AsmMethod method, String this0Class, String methodOwner) throws CannotCompileException {
        String methodName = method.getMethodName();
        Type methodType = Type.getMethodType(method.getDesc());
        String rType = methodType.getReturnType().getClassName();

        Type[] args = methodType.getArgumentTypes();
        String mhVar = methodName + "_MH_" + COUNTER.getAndIncrement();
        String argsTypeDeclare = Arrays.stream(args).map(type -> type.getClassName() + ".class").collect(Collectors.joining(","));

        String declare = String.format("public %s %s(%s)",
                rType, "super_"+methodName, IntStream.range(0, args.length).mapToObj(i -> args[i].getClassName() + " " + "var_" + i).collect(Collectors.joining(",")));
        javassistClassBuilder
                .defineField("private static final MethodHandle " + mhVar + " = LOOKUP.findSpecial(" + methodOwner + ".class,\"" + methodName + "\", " +
                        "MethodType.methodType(" + rType + ".class,new Class[]{" + argsTypeDeclare + "}), " + this0Class + ".class);")
                .defineMethod(declare + "{ throw new RuntimeException(\"not impl\"); }", (bytecode) -> {
                    bytecode.addGetstatic(javassistClassBuilder.getClassName(), mhVar, "Ljava/lang/invoke/MethodHandle;");
                    loadThis$0(bytecode, javassistClassBuilder.getClassName(), this0Class);
                    loadArgs(bytecode, args);
                    bytecode.addInvokevirtual("java/lang/invoke/MethodHandle", "invoke", AsmUtil.addArgsDesc(methodType.getDescriptor(), methodOwner, true));
                    areturn(bytecode, methodType.getReturnType());

                    bytecode.setMaxLocals(2+args.length);
                    bytecode.setMaxStack(2+AsmUtil.calculateLvbOffset(false, args));
                });
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

    public static String getUnpacking(Type type) {
        switch (type.getSort()) {
            case Type.BOOLEAN:
                return "wrap_boolean";
            case Type.CHAR:
                return "wrap_char";
            case Type.BYTE:
                return "wrap_byte";
            case Type.SHORT:
                return "wrap_short";
            case Type.INT:
                return "wrap_int";
            case Type.FLOAT:
                return "wrap_float";
            case Type.LONG:
                return "wrap_long";
            case Type.DOUBLE:
                return "wrap_double";
            case Type.VOID:
                return null;
            default:
                return "wrap_object";
        }
    }

    private static void loadArgs(Bytecode bytecode, Type[] argumentTypes) {
        int i = 0;
        for (Type argumentType : argumentTypes) {
            bytecode.add(argumentType.getOpcode(Opcodes.ILOAD), i + 1);
            if (argumentType.getSort() == Type.DOUBLE || argumentType.getSort() == Type.LONG) i++;
            i++;
        }
    }

    private static void loadThis$0(Bytecode bytecode, String this$0Holder, String this0Class) {
        bytecode.addAload(0);
        bytecode.addGetfield(this$0Holder, "this$0", AsmUtil.toTypeDesc(this0Class));
    }

    private static void areturn(Bytecode bytecode, Type rType) {
        bytecode.add(rType.getOpcode(Opcodes.IRETURN));
    }

    @SneakyThrows
    public static void fixMethodHandle(MethodInfo methodInfo, ConstPool constPool, String desc) {
        CodeAttribute ca = methodInfo.getCodeAttribute();
        CodeIterator ci = ca.iterator();

        while (ci.hasNext()) {
            int index = ci.next();
            int op = ci.byteAt(index);
            if (op == Opcode.INVOKEVIRTUAL) {
                int refIndex = ci.s16bitAt(index + 1);
                Object memberrefInfo = ReflectUtil.invokeMethod(constPool, "getItem", refIndex);
                String owner = constPool.getMethodrefClassName(refIndex);
                String name = constPool.getMethodrefName(refIndex);
                if (owner.equals("java.lang.invoke.MethodHandle") && name.equals("invoke")) {
                    Object nameAndType = ReflectUtil.invokeMethod(constPool, "getItem", ReflectUtil.getFieldValue(memberrefInfo, "nameAndTypeIndex"));
                    int descIndex = constPool.addUtf8Info(desc);
                    ReflectUtil.setFieldValue(nameAndType, "typeDescriptor", descIndex);
                }
            }
        }
    }
}
