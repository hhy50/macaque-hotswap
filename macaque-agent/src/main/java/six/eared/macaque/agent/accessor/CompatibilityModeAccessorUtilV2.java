package six.eared.macaque.agent.accessor;

import javassist.*;
import org.objectweb.asm.Type;
import six.eared.macaque.agent.asm2.AsmField;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.enhance.AccessorClassNameGenerator;
import six.eared.macaque.agent.enhance.CompatibilityModeClassLoader;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.agent.exceptions.AccessorCreateException;
import six.eared.macaque.agent.javassist.JavassistClassBuilder;
import six.eared.macaque.common.util.ClassUtil;
import six.eared.macaque.common.util.StringUtil;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


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

            AccessorClassBuilder javassistClassBuilder = generateAccessorClass(clazzDefinition.getClassName(), accessorName, superAccessor);
            collectAccessibleMethods(clazzDefinition, javassistClassBuilder, superAccessor);
            collectAccessibleFields(clazzDefinition, javassistClassBuilder, superAccessor);
            collectSuperMember(clazzDefinition, javassistClassBuilder, superAccessor);

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
    private static AccessorClassBuilder generateAccessorClass(String className, String accessorName, Accessor parentAccessor) throws NotFoundException, CannotCompileException {
        boolean containSupper = parentAccessor != null;
        AccessorClassBuilder accessorBuilder = AccessorClassBuilder.builder(Modifier.PUBLIC, accessorName,
                containSupper ? parentAccessor.getClassName() : null, null);
        if (!containSupper) {
            accessorBuilder.defineField("public java.lang.Object this$0;");
        }
        accessorBuilder.defineField("public static final MethodHandles$Lookup LOOKUP = Util.lookup("+className+".class);")
                .defineConstructor(String.format("public %s(Object this$0) { %s }", ClassUtil.toSimpleName(accessorName), containSupper?"super(this$0);":"this.this$0=this$0;"));
        accessorBuilder.setParent(parentAccessor);
        accessorBuilder.setThis$0(className);
        return accessorBuilder;
    }

    private static void collectAccessibleMethods(ClazzDefinition definition, AccessorClassBuilder accessorBuilder, Accessor superAccessor) {
        try {
            // 收集自身全部的方法
            for (AsmMethod method : definition.getAsmMethods()) {
                if (method.isConstructor() || method.isClinit()) {
                    continue;
                }
                accessorBuilder.addMethod(definition.getClassName(), method);
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
        } catch (Exception e) {
            throw new AccessorCreateException(e);
        }
    }

    private static void collectSuperMember(ClazzDefinition definition, AccessorClassBuilder accessorBuilder, Accessor superAccessor) throws IOException, ClassNotFoundException, CannotCompileException {
        // 收集父类中所有可以访问到的方法
        ClazzDefinition superClassDefinition = AsmUtil.readOriginClass(definition.getSuperClassName());
        for (AsmMethod superMethod : superClassDefinition.getAsmMethods()) {
            if (superMethod.isConstructor() || superMethod.isClinit() || superMethod.isPrivate()) {
                continue;
            }
            accessorBuilder.addMethod(superClassDefinition.getClassName(), superMethod);
        }
        if (superAccessor == null) {
            String superClass = superClassDefinition.getSuperClassName();
            while (superClass != null) {
                superClassDefinition = AsmUtil.readOriginClass(superClass);
                for (AsmMethod superMethod : superClassDefinition.getAsmMethods()) {
                    if (superMethod.isConstructor() || superMethod.isClinit() || superMethod.isPrivate()) {
                        continue;
                    }
                    accessorBuilder.addMethod(superClassDefinition.getClassName(), superMethod);
                }
                superClass = superClassDefinition.getSuperClassName();
            }
        }

        // default method in interface class
        if (Environment.getJdkVersion() > 7) {

        }
    }

    /**
     * 为私有字段和实例字段生成访问方法
     * 排除非私有的静态(ps: 非私有的静态可以在任意地方访问,所以不需要访问方法)
     *
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
            String mhVar = name+"_getter_mh_"+COUNTER.getAndIncrement();
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

                        bytecode.setMaxLocals(asmField.isStatic()?0:1); // 1=this
                        bytecode.setMaxStack(asmField.isStatic()?1:2); // 2=mh+this$0, 静态没有this$0
                    });
        } else if (!asmField.isStatic()) {
            javassistClassBuilder.defineMethod(declare+"{ return "+("(("+this$0+") this$0)."+name)+"; }");
        }
    }

    /**
     * 为私有字段和实例字段生成访问方法
     * 排除非私有的静态(ps: 非私有的静态可以在任意地方访问,所以不需要访问方法)
     *
     * @param javassistClassBuilder
     * @param asmField
     * @param this$0
     * @param this$0Holder
     * @throws CannotCompileException
     */
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
        } else if (!asmField.isStatic()){
            javassistClassBuilder.defineMethod(declare+"{"+this$0+"."+name+"=arg;}");
        }
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
