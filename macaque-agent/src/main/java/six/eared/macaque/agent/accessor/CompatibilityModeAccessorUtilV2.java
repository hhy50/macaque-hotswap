package six.eared.macaque.agent.accessor;

import javassist.*;
import javassist.bytecode.*;
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
                .defineField("public static final MethodHandles$Lookup LOOKUP = Util.lookup("+className+".class);");
        if (!containSupper) {
            javassistClassBuilder.defineField(Modifier.PUBLIC | AccessFlag.SYNTHETIC, "this$0", "java.lang.Object");
        }
        javassistClassBuilder.defineConstructor(String.format("public %s(Object this$0) { %s }",
                ClassUtil.toSimpleName(accessorName), containSupper?"super(this$0);":"this.this$0=this$0;"));
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
            String this$0Holder = accessorBuilder.getClassName();
            String this$0 = definition.getClassName();

            // my all method
            for (AsmMethod method : definition.getAsmMethods()) {
                if (method.isConstructor() || method.isClinit()) {
                    continue;
                }
                if (method.isStatic()) {
                    invokerStatic(accessorBuilder, method, definition.getClassName());
                } else if (method.isPrivate()) {
                    invokeSpecial(accessorBuilder, method, definition.getClassName(), this$0, this$0Holder);
                } else {
                    // 继承而来 （如果自己重写了父类的方法, 就保存父类的字节码，防止 super调用）
                    boolean inherited = inherited(definition.getSuperClassName(), method.getMethodName(), method.getDesc());
                    if (inherited && superAccessor != null) {
                        continue;
                    }
                    // 不是继承而来的 或者 继承来的但是没有父accessor, 就生成方法调用
                    invokerVirtual(accessorBuilder, method, definition.getClassName());
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
                    String key = superMethod.getMethodName()+"#"+superMethod.getDesc();
                    if (!unique.contains(key)) {
                        unique.add(superMethod.getMethodName()+"#"+superMethod.getDesc());
                        invokeSpecial(accessorBuilder, superMethod, superClassDefinition.getClassName(), this$0, this$0Holder);
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
            String this$0Holder = definition.getClassName();
            // my all field
            for (AsmField asmField : definition.getAsmFields()) {
                getField(javassistClassBuilder, asmField, definition.getClassName(), this$0Holder);
                if (!asmField.isFinal()) {
                    setField(javassistClassBuilder, asmField, definition.getClassName(), this$0Holder);
                }
            }
            // non private field in super class


        } catch (Exception e) {
            throw new AccessorCreateException(e);
        }
    }

    /**
     * @param javassistClassBuilder
     * @param asmField
     * @param this$0
     * @param this$0Holder
     * @throws CannotCompileException
     */
    private static void getField(JavassistClassBuilder javassistClassBuilder, AsmField asmField, String this$0, String this$0Holder) throws CannotCompileException {
        Type fieldType = Type.getType(asmField.getDesc());
        String type = fieldType.getClassName();
        String name = asmField.getFieldName();
        String declare = "public "+(asmField.isStatic()?"static ":"")+type+" "+Accessor.FIELD_GETTER_PREFIX+name+"()";
        if (asmField.isPrivate()) {
            String mhVar = name+"_get_mh_"+COUNTER.getAndIncrement();
            javassistClassBuilder
                    .defineField("private static final MethodHandle "+mhVar+"=LOOKUP."+(asmField.isStatic()?"findStaticGetter":"findGetter")+
                            "("+this$0+".class, \""+name+"\", "+fieldType.getClassName()+".class);")
                    .defineMethod(declare+"{ throw new RuntimeException(\"not impl\"); }", (bytecode) -> {
                        String dynamicDesc = "()"+asmField.getDesc();
                        if (!asmField.isStatic()) {
                            loadThis$0(bytecode, this$0Holder, this$0);
                            dynamicDesc = AsmUtil.addArgsDesc(dynamicDesc, this$0, true);
                        }
                        bytecode.addGetstatic(javassistClassBuilder.getClassName(), mhVar, "Ljava/lang/invoke/MethodHandle;");
                        bytecode.addInvokevirtual("java/lang/invoke/MethodHandle", "invoke", dynamicDesc);
                        areturn(bytecode, fieldType);

                        bytecode.setMaxLocals(asmField.isStatic()?0:1);
                        bytecode.setMaxStack(2);
                    });
        } else {
            String body = asmField.isStatic()?(this$0+"."+name):("(("+this$0+") this$0)."+name);
            javassistClassBuilder.defineMethod(declare+"{ return "+body+"; }");
        }
    }

    private static void setField(JavassistClassBuilder javassistClassBuilder, AsmField asmField, String this$0, String this$0Holder) throws CannotCompileException {
        Type fieldType = Type.getType(asmField.getDesc());
        String type = fieldType.getClassName();
        String name = asmField.getFieldName();

        String declare = "public "+(asmField.isStatic()?"static ":"")+"void "+Accessor.FIELD_SETTER_PREFIX+name+"("+type+" arg)";
        if (asmField.isPrivate()) {
            String mhVar = name+"_set_mh_"+COUNTER.getAndIncrement();
            javassistClassBuilder
                    .defineField("private static final MethodHandle "+mhVar+"=LOOKUP."+(asmField.isStatic()?"findStaticSetter":"findSetter")+
                            "("+this$0+".class, \""+name+"\", "+fieldType.getClassName()+".class);")
                    .defineMethod(declare+"{ throw new RuntimeException(\"not impl\"); }", (bytecode) -> {
                        String dynamicDesc = "()"+asmField.getDesc();
                        if (!asmField.isStatic()) {
                            loadThis$0(bytecode, this$0Holder, this$0);
                            dynamicDesc = AsmUtil.addArgsDesc(dynamicDesc, this$0, true);
                        }
                        bytecode.addGetstatic(javassistClassBuilder.getClassName(), mhVar, "Ljava/lang/invoke/MethodHandle;");
                        bytecode.addInvokevirtual("java/lang/invoke/MethodHandle", "invoke", dynamicDesc);
                        areturn(bytecode, fieldType);

                        int lvb = AsmUtil.calculateLvbOffset(asmField.isStatic(), new Type[] {fieldType});
                        bytecode.setMaxLocals(lvb);
                        bytecode.setMaxLocals(lvb);
                    });
        } else {
            String body = (asmField.isStatic()?this$0+"."+name:"(("+this$0+") this$0)."+name)+"=arg;";
            javassistClassBuilder.defineMethod(declare+"{ "+body+" }");
        }
    }

    /**
     * @param javassistClassBuilder 构造器
     * @param method                生成的方法, 这里的方法应该是属于this$0的
     * @param this$0                this$0 的类名
     */
    private static void invokerVirtual(JavassistClassBuilder javassistClassBuilder, AsmMethod method, String this$0) throws CannotCompileException {
        String methodName = method.getMethodName();
        Type methodType = Type.getMethodType(method.getDesc());
        Type[] args = methodType.getArgumentTypes();
        String[] argVars = IntStream.range(0, args.length).mapToObj(i -> "var_"+i).toArray(String[]::new);
        String rType = methodType.getReturnType().getClassName();

        String declare = "public "+rType+" "+methodName+"("+
                IntStream.range(0, args.length).mapToObj(i -> args[i].getClassName()+" "+argVars[i]).collect(Collectors.joining(","))+")";
        String body = (rType.equals("void")?"":"return ("+rType+")")+
                "(("+this$0+") this$0)."+methodName+"("+String.join(",", argVars)+");";
        javassistClassBuilder.defineMethod(declare+"{"+body+"}");
    }


    /**
     * @param javassistClassBuilder 构造器
     * @param method                生成的方法, 这里的方法应该是属于this$0的
     * @param this$0                this$0 的类名
     */
    private static void invokerStatic(JavassistClassBuilder javassistClassBuilder, AsmMethod method, String this$0) throws CannotCompileException {
        String methodName = method.getMethodName();
        Type methodType = Type.getMethodType(method.getDesc());
        Type[] args = methodType.getArgumentTypes();
        String[] argVars = IntStream.range(0, args.length).mapToObj(i -> "var_"+i).toArray(String[]::new);
        String rType = methodType.getReturnType().getClassName();

        String declare = String.format("public static %s %s(%s)",
                rType, methodName, IntStream.range(0, args.length).mapToObj(i -> args[i].getClassName()+" "+argVars[i]).collect(Collectors.joining(",")));
        if (method.isPrivate()) {
            String mhVar = methodName+"_mh_"+COUNTER.getAndIncrement();
            String argsTypeDeclare = Arrays.stream(args).map(type -> type.getClassName()+".class").collect(Collectors.joining(","));
            javassistClassBuilder
                    .defineField("private static final MethodHandle "+mhVar+" = LOOKUP.findStatic("+this$0+".class,\""+methodName+"\", "+
                            "MethodType.methodType("+rType+".class,new Class[]{"+argsTypeDeclare+"}));")
                    .defineMethod(declare+"{ throw new RuntimeException(\"not impl\"); }", (bytecode) -> {
                        bytecode.addGetstatic(javassistClassBuilder.getClassName(), mhVar, "Ljava/lang/invoke/MethodHandle;");
                        loadArgs(bytecode, args);
                        bytecode.addInvokevirtual("java/lang/invoke/MethodHandle", "invoke", methodType.getDescriptor());
                        areturn(bytecode, methodType.getReturnType());

                        int lvb = AsmUtil.calculateLvbOffset(true, args);
                        bytecode.setMaxLocals(lvb);
                        bytecode.setMaxStack(2+lvb);
                    });
        } else {
            String body = (rType.equals("void")?"":"return ("+rType+")")+
                    this$0+"."+methodName+"("+String.join(",", argVars)+");";
            javassistClassBuilder.defineMethod(declare+"{"+body+"}");
        }
    }

    /**
     * @param javassistClassBuilder 构造器
     * @param method                生成的方法, 这里的方法应该是属于this$0的
     * @param this$0                this$0的类名
     * @param this$0Holder          持有this$0的类名
     */
    private static void invokeSpecial(JavassistClassBuilder javassistClassBuilder, AsmMethod method, String methodOwner, String this$0, String this$0Holder) throws CannotCompileException {
        String methodName = method.getMethodName();
        String pre = this$0.replace('.', '_');
        Type methodType = Type.getMethodType(method.getDesc());
        String rType = methodType.getReturnType().getClassName();

        Type[] args = methodType.getArgumentTypes();
        String mhVar = methodName+"_mh_"+COUNTER.getAndIncrement();
        String argsTypeDeclare = Arrays.stream(args).map(type -> type.getClassName()+".class").collect(Collectors.joining(","));

        String declare = String.format("public %s %s(%s)",
                rType, pre+"_"+methodName, IntStream.range(0, args.length).mapToObj(i -> args[i].getClassName()+" "+"var_"+i).collect(Collectors.joining(",")));
        javassistClassBuilder
                .defineField("private static final MethodHandle "+mhVar+" = LOOKUP.findSpecial("+methodOwner+".class,\""+methodName+"\", "+
                        "MethodType.methodType("+rType+".class,new Class[]{"+argsTypeDeclare+"}), "+this$0+".class);")
                .defineMethod(declare+"{ throw new RuntimeException(\"not impl\"); }", (bytecode) -> {
                    bytecode.addGetstatic(javassistClassBuilder.getClassName(), mhVar, "Ljava/lang/invoke/MethodHandle;");
                    loadThis$0(bytecode, this$0Holder, this$0);
                    loadArgs(bytecode, args);
                    bytecode.addInvokevirtual("java/lang/invoke/MethodHandle", "invoke", AsmUtil.addArgsDesc(methodType.getDescriptor(), this$0, true));
                    areturn(bytecode, methodType.getReturnType());

                    int lvb = AsmUtil.calculateLvbOffset(false, args);
                    bytecode.setMaxLocals(1+lvb);
                    bytecode.setMaxStack(2+lvb);
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

    private static void loadArgs(Bytecode bytecode, Type[] argumentTypes) {
        int i = 0;
        for (Type argumentType : argumentTypes) {
            bytecode.add(argumentType.getOpcode(Opcodes.ILOAD), i+1);
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
}
