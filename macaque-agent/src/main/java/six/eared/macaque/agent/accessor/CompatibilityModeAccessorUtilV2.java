package six.eared.macaque.agent.accessor;

import javassist.CannotCompileException;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import six.eared.macaque.agent.asm2.AsmField;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.enhance.AccessorClassNameGenerator;
import six.eared.macaque.agent.enhance.CompatibilityModeClassLoader;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.agent.exceptions.AccessorCreateException;
import six.eared.macaque.common.util.ClassUtil;
import six.eared.macaque.common.util.StringUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
            Accessor accessor = javassistClassBuilder.toAccessor();
            LOADED.put(className, accessor);
            return accessor;
        } catch (Exception e) {
            throw new AccessorCreateException(e);
        }
    }


    /**
     * @param className
     * @param accessorName
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

    private static void collectAccessibleFields(ClazzDefinition definition, AccessorClassBuilder accessorBuilder, Accessor superAccessor) {
        try {
            // my all field
            for (AsmField asmField : definition.getAsmFields()) {
                accessorBuilder.addField(definition.getClassName(), asmField);
            }
        } catch (Exception e) {
            throw new AccessorCreateException(e);
        }
    }

    private static void collectSuperMember(ClazzDefinition definition, AccessorClassBuilder accessorBuilder, Accessor superAccessor) throws IOException, ClassNotFoundException, CannotCompileException, BadBytecode, NotFoundException {
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
