package six.eared.macaque.agent.accessor;

import javassist.CannotCompileException;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import org.objectweb.asm.Type;
import six.eared.macaque.agent.asm2.AsmField;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.ClassBuilder;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.enhance.AccessorClassNameGenerator;
import six.eared.macaque.agent.enhance.CompatibilityModeClassLoader;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.agent.exceptions.AccessorCreateException;
import six.eared.macaque.agent.javassist.JavaSsistUtil;
import six.eared.macaque.common.util.ClassUtil;
import six.eared.macaque.common.util.CollectionUtil;
import six.eared.macaque.common.util.StringUtil;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class CompatibilityModeAccessorUtil {

    private static final Map<String, ClazzDefinition> LOADED = new HashMap<>();

    /**
     * @param className          外部类类名
     * @param classNameGenerator 类名生成器
     * @param deepth             深度
     * @return
     */
    public static ClazzDefinition createAccessor(String className, AccessorClassNameGenerator classNameGenerator, int deepth) {
        if (LOADED.containsKey(className)) {
            return LOADED.get(className);
        }
        String accessorName = classNameGenerator.generate(className);
        try {
            ClazzDefinition clazzDefinition = AsmUtil.readOriginClass(className);
            String superClassName = clazzDefinition.getSuperClassName();
            ClazzDefinition superAccessor = null;
            if (deepth > 0) {
                if (StringUtil.isNotEmpty(superClassName)
                        && !isSystemClass(superClassName)) {
                    superAccessor = createAccessor(superClassName, classNameGenerator, --deepth);
                }
            }
            String superAccessorName = tryGetAccessorClassName(superClassName, classNameGenerator);
            ClassBuilder classBuilder = generateAccessorClass(accessorName, superAccessorName);

            collectAccessibleMethods(clazzDefinition, classBuilder, superAccessor);
            collectAccessibleFields(clazzDefinition, classBuilder, superAccessor);
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
     * @param superAccessorName
     * @return
     */
    private static ClassBuilder generateAccessorClass(String accessorName, String superAccessorName) throws NotFoundException, CannotCompileException {
        boolean containSupper = superAccessorName != null;
        ClassBuilder classBuilder
                = JavaSsistUtil.defineClass(Modifier.PUBLIC, accessorName, superAccessorName, null);
        classBuilder.defineField("public static final MethodHandles$Lookup LOOKUP = MethodHandles.lookup();");
        if (!containSupper) {
            classBuilder.defineField(Modifier.PUBLIC | AccessFlag.SYNTHETIC, "this$0", "java.lang.Object");
        }
        classBuilder.defineConstructor(String.format("public %s(Object this$0) { %s }",
                ClassUtil.toSimpleName(accessorName), containSupper ? "super(this$0);" : "this.this$0=this$0;"));
        return classBuilder;
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

    private static void collectAccessibleMethods(ClazzDefinition definition, ClassBuilder accessorBuilder, ClazzDefinition superAccessor) {
        try {
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
                invokerVirtual(accessorBuilder, definition.getClassName(), method);
            }

            // non private method in super class
            String superClassName = definition.getSuperClassName();
            ClazzDefinition superClassDefinition = AsmUtil.readOriginClass(superClassName);
            while (superClassDefinition != null) {
                for (AsmMethod superMethod : superClassDefinition.getAsmMethods()) {
                    if (superMethod.isConstructor() || superMethod.isClinit() || superMethod.isPrivate()) {
                        continue;
                    }
                    if (!accessibleSuperMethods.containsKey(superMethod.getUniqueDesc())) {
                        accessibleSuperMethods.put(superMethod.getUniqueDesc(), superMethod);
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
            if (CollectionUtil.isNotEmpty(privateMethods)) {
                for (AsmMethod privateMethod : privateMethods) {
                    invokeSpecial(definition.getClassName(), privateMethod, accessorBuilder);
                }
            }
            if (accessibleSuperMethods.size() > 0) {
                for (Map.Entry<String, AsmMethod> methodEntry : accessibleSuperMethods.entrySet()) {
                    AsmMethod superMethod = methodEntry.getValue();
                    // TODO 多态调用
                    invokeSpecial(definition.getClassName(), superMethod, accessorBuilder);
                }
            }
        } catch (Exception e) {
            throw new AccessorCreateException(e);
        }
    }

    private static void collectAccessibleFields(ClazzDefinition definition, ClassBuilder classBuilder, ClazzDefinition superAccessor) {
        try {
            // my all field
            for (AsmField asmField : definition.getAsmFields()) {
                getField(asmField, definition.getClassName(), classBuilder);
            }
            // non private field in super class


        } catch (Exception e) {
            throw new AccessorCreateException(e);
        }
    }

    private static void getField(AsmField asmField, String owner, ClassBuilder classBuilder) throws CannotCompileException {
        Type fieldType = Type.getType(asmField.getDesc());
        String type = fieldType.getClassName();
        String name = asmField.getFieldName();
        String unpacking = getUnpacking(fieldType);
        String body = null;

        if (asmField.isPrivate()) {
            body = "Field field = " + owner + ".class.getDeclaredField(\"" + name + "\"); field.setAccessible(true);" +
                    "return ((" + type + ") Util." + unpacking + "(field.get(this$0)));";
        } else {
            body = "return ((" + owner + ") this$0)." + name + ";";
        }
        classBuilder.defineMethod(String.format("public %s "+ Accessor.FIELD_GETTER_PREFIX+"%s() { %s }", type, name, body));
    }

    private static void invokerVirtual(ClassBuilder classBuilder, String this0Class,
                                       AsmMethod method) throws CannotCompileException {
        String methodName = method.getMethodName();
        Type methodType = Type.getMethodType(method.getDesc());
        Type[] args = methodType.getArgumentTypes();

        String rType = methodType.getReturnType().getClassName();
        String[] argVars = IntStream.range(0, args.length).mapToObj(i -> "var_" + i).toArray(String[]::new);

        String declare = String.format("public %s %s(%s)",
                rType, methodName, IntStream.range(0, args.length).mapToObj(i -> args[i].getClassName() + " " + argVars[i]).collect(Collectors.joining(",")));

        String body = (rType.equals("void") ? "" : "return (" + rType + ")")
                + " ((" + this0Class + ") this$0)." + methodName + "(" + String.join(",", argVars) + ");";
        classBuilder.defineMethod(declare + "{" + body + "}");
    }

    /**
     * @param this0Class   this0Class
     * @param method       生成的方法
     * @param classBuilder 构造器
     */
    private static void invokeSpecial(String this0Class, AsmMethod method, ClassBuilder classBuilder) throws CannotCompileException {
        String methodName = method.getMethodName();
        String methodClass = method.getClassName();
        Type methodType = Type.getMethodType(method.getDesc());
        String rType = methodType.getReturnType().getClassName();

        Type[] args = methodType.getArgumentTypes();
        String[] argVars = IntStream.range(0, args.length).mapToObj(i -> "var_" + i).toArray(String[]::new);
        String argsClassDeclare = Arrays.stream(args).map(type -> type.getClassName() + ".class").collect(Collectors.joining(","));
        String argsDeclare = IntStream.range(0, args.length).mapToObj(i -> args[i].getClassName() + " " + argVars[i])
                .collect(Collectors.joining(","));
        String[] packingArgs = Arrays.stream(argVars).map(a -> "Util.packing(" + a + ")").toArray(String[]::new);
        String unpacking = getUnpacking(methodType.getReturnType());

        StringBuilder methodSrc = new StringBuilder("public " + rType + " super_" + methodName + "(" + argsDeclare + ") {").append("\n")
                .append("MethodType type = MethodType.methodType(" + rType + ".class,new Class[]{" + argsClassDeclare + "});").append("\n")
                .append("MethodHandle mh = LOOKUP.findSpecial(" + this0Class + ".class,\"" + methodName + "\",type," + methodClass + ".class).bindTo(this$0);").append("\n")
                .append(unpacking != null ? "return (" + rType + ")" : "")
                .append(unpacking != null ? "Util." + unpacking + "(" : "(")
                .append("mh.invoke(new Object[] {" + String.join(",", packingArgs) + "}));").append("\n")
                .append("}");
        classBuilder.defineMethod(methodSrc.toString());
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
                return "unpack_boolean";
            case Type.CHAR:
                return "unpack_char";
            case Type.BYTE:
                return "unpack_byte";
            case Type.SHORT:
                return "unpack_short";
            case Type.INT:
                return "unpack_int";
            case Type.FLOAT:
                return "unpack_float";
            case Type.LONG:
                return "unpack_long";
            case Type.DOUBLE:
                return "unpack_double";
            case Type.VOID:
                return null;
            default:
                return "unpack_object";
        }
    }
}
