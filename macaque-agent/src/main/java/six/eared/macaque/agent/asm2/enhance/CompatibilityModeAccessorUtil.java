package six.eared.macaque.agent.asm2.enhance;

import javassist.CannotCompileException;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import six.eared.macaque.agent.asm2.AsmField;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.ClassBuilder;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.agent.exceptions.AccessorCreateException;
import six.eared.macaque.agent.javassist.JavaSsistUtil;
import six.eared.macaque.asm.MethodVisitor;
import six.eared.macaque.asm.Opcodes;
import six.eared.macaque.asm.Type;
import six.eared.macaque.common.util.ClassUtil;
import six.eared.macaque.common.util.CollectionUtil;
import six.eared.macaque.common.util.StringUtil;

import java.io.IOException;
import java.lang.reflect.Field;
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
                                                      String superAccessorName) throws NotFoundException, CannotCompileException {
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
    private static String tryGetAccessorClassName(String className, ClassNameGenerator classNameGenerator) {
        if (LOADED.containsKey(className)) {
            return LOADED.get(className).getClassName();
        }
        return null;
    }

    private static void collectAccessibleMethods(ClazzDefinition definition, ClassBuilder accessorBuilder, ClazzDefinition superAccessor,
                                                 ClassNameGenerator classNameGenerator) {
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
     * <p>
     *      MethodType type = MethodType.methodType({arg0...n});
     *      MethodHandle mh = LOOKUP.findSpecial({@param this0Class}.class, "{@param method.name}", type, {@param superClassName}.class)
     *                              .bindTo(this$0);
     *      return mh.invoke({arg0...n});
     * </p>
     */
    /**
     * @param this0Class   this0Class
     * @param method       生成的方法
     * @param classBuilder 构造器
     */
    private static void invokeSpecial(String this0Class, AsmMethod method, ClassBuilder classBuilder) throws CannotCompileException {
        String methodName = method.getMethodName();
        Type methodType = Type.getMethodType(method.getDesc());
        String rType = methodType.getReturnType().getClassName();
        Type[] args = methodType.getArgumentTypes();
        String[] argVars = IntStream.range(0, args.length).mapToObj(i -> "var_" + i).toArray(String[]::new);

        String declare = String.format("public %s %s(%s)",
                rType, "super_" + methodName, IntStream.range(0, args.length).mapToObj(i -> args[i].getClassName() + " " + argVars[i]).collect(Collectors.joining(",")));
        String body = "MethodType type = MethodType.methodType(" + rType + ".class, " + Arrays.stream(args).map(type -> type.getClassName() + ".class").collect(Collectors.joining(", ")) + ");" +
                "MethodHandle mh = LOOKUP.findSpecial(" + this0Class + ".class, \"" + methodName + "\", type, " + method.getClassName() + ".class).bindTo(this$0);" +
                (rType.equals("void") ? "" : "return (" + rType + ") ") +
                "mh.invoke(new Object[] {" + String.join(", ", argVars) + "});";

        classBuilder.defineMethod(declare + "{" + body + "}");
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
}
