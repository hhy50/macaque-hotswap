package six.eared.macaque.agent.accessor;

import javassist.CannotCompileException;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
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
import six.eared.macaque.common.util.StringUtil;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class CompatibilityModeAccessorUtilV2 {

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
                .defineField("public static final MethodHandles$Lookup LOOKUP;")
                .defineStaticBlock("{" +
                        "Constructor constructor = MethodHandles$Lookup.class.getDeclaredConstructors()[0];" +
                        "constructor.setAccessible(true); " +
                        "LOOKUP = (MethodHandles$Lookup) constructor.newInstance(new Object[]{" + className + ".class});" +
                        "}");
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
                    invokeSpecial(definition.getClassName(), definition.getClassName(), method, accessorBuilder);
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
                        invokeSpecial(definition.getClassName(), superClassDefinition.getClassName(), superMethod, accessorBuilder);
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
        javassistClassBuilder.defineMethod(String.format("public "+(asmField.isStatic()?"static ":"")+"void " + Accessor.FIELD_SETTER_PREFIX + "%s(%s arg) { %s }", name, type, body));
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
            String argsClassDeclare = Arrays.stream(args).map(type -> type.getClassName() + ".class").collect(Collectors.joining(","));
            String[] packingArgs = Arrays.stream(argVars).map(a -> "Util.wrapping(" + a + ")").toArray(String[]::new);
            String unpacking = getUnpacking(methodType.getReturnType());

            body = new StringBuilder()
                    .append("MethodType type = MethodType.methodType(" + rType + ".class,new Class[]{" + argsClassDeclare + "});").append("\n")
                    .append("MethodHandle mh = LOOKUP.findStatic(" + this0Class + ".class,\"" + methodName + "\",type);").append("\n")
                    .append(unpacking != null ? "return (" + rType + ")" : "")
                    .append(unpacking != null ? "Util." + unpacking + "(" : "(")
                    .append("mh.invoke(new Object[] {" + String.join(",", packingArgs) + "}));").toString();
        } else {
            body = (rType.equals("void") ? "" : "return (" + rType + ")") +
                    this0Class + "." + methodName + "(" + String.join(",", argVars) + ");";
        }
        javassistClassBuilder.defineMethod(declare + "{" + body + "}");
    }

    /**
     * @param this0Class            this0Class
     * @param method                生成的方法
     * @param javassistClassBuilder 构造器
     */
    private static void invokeSpecial(String this0Class, String methodOwner, AsmMethod method, JavassistClassBuilder javassistClassBuilder) throws CannotCompileException {
        String methodName = method.getMethodName();
        Type methodType = Type.getMethodType(method.getDesc());
        String rType = methodType.getReturnType().getClassName();

        Type[] args = methodType.getArgumentTypes();
        String[] argVars = IntStream.range(0, args.length).mapToObj(i -> "var_" + i).toArray(String[]::new);
        String argsClassDeclare = Arrays.stream(args).map(type -> type.getClassName() + ".class").collect(Collectors.joining(","));
        String argsDeclare = IntStream.range(0, args.length).mapToObj(i -> args[i].getClassName() + " " + argVars[i])
                .collect(Collectors.joining(","));
        String[] packingArgs = Arrays.stream(argVars).map(a -> "Util.wrapping(" + a + ")").toArray(String[]::new);
        String unpacking = getUnpacking(methodType.getReturnType());

        StringBuilder methodSrc = new StringBuilder("public " + rType + " super_" + methodName + "(" + argsDeclare + ") {").append("\n")
                .append("MethodType type = MethodType.methodType(" + rType + ".class,new Class[]{" + argsClassDeclare + "});").append("\n")
                .append("MethodHandle mh = LOOKUP.findSpecial(" + methodOwner + ".class,\"" + methodName + "\",type," + this0Class + ".class).bindTo(this$0);").append("\n")
                .append(unpacking != null ? "return (" + rType + ")" : "")
                .append(unpacking != null ? "Util." + unpacking + "(" : "(")
                .append("mh.invoke(new Object[] {" + String.join(",", packingArgs) + "}));").append("\n")
                .append("}");
        javassistClassBuilder.defineMethod(methodSrc.toString());
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
}
