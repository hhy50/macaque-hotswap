package six.eared.macaque.agent.accessor;

import io.github.hhy50.linker.asm.AsmClassBuilder;
import six.eared.macaque.agent.asm2.AsmField;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.enhance.AccessorClassNameGenerator;
import six.eared.macaque.agent.enhance.EnhanceBytecodeClassLoader;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.agent.exceptions.AccessorCreateException;
import six.eared.macaque.common.util.StringUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static six.eared.macaque.common.util.ClassUtil.isSystemClass;


public class AccessorUtil {

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

            AccessorClassBuilder accessorClassBuilder = generateAccessorClass(clazzDefinition.getClassName(), accessorName, superAccessor);
            collectAccessibleMethods(clazzDefinition, accessorClassBuilder, superAccessor);
            collectAccessibleFields(clazzDefinition, accessorClassBuilder, superAccessor);
            collectSuperMember(clazzDefinition, accessorClassBuilder, superAccessor);

            Accessor accessor = ((AccessorClassBuilder) accessorClassBuilder.end()).toAccessor();
            AsmClassBuilder linker = accessorClassBuilder.getLinkerClassBuilder().end();
            EnhanceBytecodeClassLoader.loadClass(linker.getClassName(), linker.toBytecode());
            EnhanceBytecodeClassLoader.loadClass(accessorClassBuilder.getClassName(), accessorClassBuilder.toBytecode());
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
    private static AccessorClassBuilder generateAccessorClass(String className, String accessorName, Accessor parentAccessor) {
        boolean containSupper = parentAccessor != null;
        AccessorClassBuilder accessorBuilder = new AccessorClassBuilder(accessorName, containSupper ? parentAccessor.getClassName() : null, null);
        accessorBuilder.setThis$0(className)
                .setParent(parentAccessor);
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

    private static void collectSuperMember(ClazzDefinition definition, AccessorClassBuilder accessorBuilder, Accessor superAccessor) throws IOException, ClassNotFoundException {
        // 收集父类中所有可以访问到的方法
        ClazzDefinition superClassDefinition = AsmUtil.readOriginClass(definition.getSuperClassName());
        doCollectSuperMember(accessorBuilder, superClassDefinition);

        if (superAccessor == null) {
            String superClass = superClassDefinition.getSuperClassName();
            while (superClass != null) {
                superClassDefinition = AsmUtil.readOriginClass(superClass);
                doCollectSuperMember(accessorBuilder, superClassDefinition);
                superClass = superClassDefinition.getSuperClassName();
            }
        }
        // default method in interface class
        if (Environment.getJdkVersion() > 7) {

        }
    }

    private static void doCollectSuperMember(AccessorClassBuilder accessorBuilder, ClazzDefinition superClassDefinition) {
        for (AsmMethod superMethod : superClassDefinition.getAsmMethods()) {
            if (superMethod.isConstructor() || superMethod.isClinit()) {
                continue;
            }
            if (superMethod.isPublicForSub()) {
                accessorBuilder.addMethod(superClassDefinition.getClassName(), superMethod);
            }
        }
        for (AsmField superField : superClassDefinition.getAsmFields()) {
            if (superField.isPublicForSub()) {
                accessorBuilder.addField(superClassDefinition.getClassName(), superField);
            }
        }
    }
}
