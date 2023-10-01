package six.eared.macaque.agent.asm2.enhance;

import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.ClassBuilder;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.asm2.classes.FieldDesc;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.asm.Opcodes;
import six.eared.macaque.common.util.ClassUtil;
import six.eared.macaque.common.util.CollectionUtil;
import six.eared.macaque.common.util.StringUtil;

import java.util.HashSet;
import java.util.Set;

public class CompatibilityModeAccessorUtil {

    public static String createAccessor(ClazzDefinition definition, ClassNameGenerator classNameGenerator, int deepth) {
        String superAccessorName = null;
        String superClassName = definition.getSuperClassName();
        if (deepth > 0) {
            if (StringUtil.isNotEmpty(superClassName)
                    && !isSystemClass(superClassName)) {
                try {
                    superAccessorName = createAccessor(AsmUtil.readOriginClass(superClassName), classNameGenerator, --deepth);
                } catch (ClassNotFoundException e) {
                    if (Environment.isDebug()) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (superAccessorName == null && StringUtil.isNotEmpty(superClassName)) {
            superAccessorName = tryGetAccessorClassName(superClassName, classNameGenerator);
        }
        Set<FieldDesc> accessibleFields = collectAccessibleFields(definition, superAccessorName != null);
        Set<AsmMethod> accessibleMethods = collectAccessibleMethods(definition, superAccessorName != null);
        return generatorAndLoad(definition, accessibleFields, accessibleMethods, superAccessorName, classNameGenerator);
    }

    /**
     * @param definition
     * @param fieldDescSet
     * @param methodDescSet
     * @param superAccessorName
     * @param classNameGenerator
     * @return
     */
    private static String generatorAndLoad(ClazzDefinition definition,
                                           Set<FieldDesc> fieldDescSet, Set<AsmMethod> methodDescSet,
                                           String superAccessorName, ClassNameGenerator classNameGenerator) {

        String innerAccessorName = classNameGenerator.generateInnerAccessorName(definition.getClassName());
        String superClassDesc = "L" + ClassUtil.simpleClassName2path(definition.getClassName()) + ";";

        ClassBuilder classBuilder = AsmUtil
                .defineClass(Opcodes.ACC_PUBLIC, innerAccessorName, superAccessorName, null, null)
                .defineField(Opcodes.ACC_SYNTHETIC | Opcodes.ACC_FINAL, "this$0", superClassDesc, null, null)
                .defineConstruct(Opcodes.ACC_PUBLIC, new String[]{definition.getClassName()}, null, null)
                .accept(visitor -> {
                    visitor.visitVarInsn(Opcodes.ALOAD, 0);
                    visitor.visitVarInsn(Opcodes.ALOAD, 1);
                    visitor.visitFieldInsn(Opcodes.PUTFIELD, ClassUtil.simpleClassName2path(innerAccessorName), "this$0", superClassDesc);
                    visitor.visitVarInsn(Opcodes.ALOAD, 0);
                    visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
                    visitor.visitInsn(Opcodes.RETURN);
                    visitor.visitEnd();
                });

        if (CollectionUtil.isNotEmpty(methodDescSet)) {
            for (AsmMethod method : methodDescSet) {
                if (method.getMethodName().equals("<init>")) {
                    continue;
                }
                if ((method.getModifier() | Opcodes.ACC_PRIVATE) > 0) {
                    continue;
                }
                classBuilder
                        .defineMethod(Opcodes.ACC_PUBLIC, method.getMethodName(), method.getDesc(), method.getExceptions(), method.getMethodSign())
                        .accept(visitor -> {
                            visitor.visitVarInsn(Opcodes.ALOAD, 0);
                            visitor.visitFieldInsn(Opcodes.GETFIELD,
                                    ClassUtil.simpleClassName2path(innerAccessorName), "this$0", superClassDesc);
                            visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                    ClassUtil.simpleClassName2path(definition.getClassName()), method.getMethodName(), method.getDesc(), false);
                            visitor.visitInsn(Opcodes.ARETURN);
                            visitor.visitMaxs(1, 1);
                        });
            }
        }
        byte[] innerBytecode = classBuilder.toByteArray();
        CompatibilityModeClassLoader.loadClass(innerAccessorName, innerBytecode);
        return innerAccessorName;
    }

    /**
     * @param className
     * @param classNameGenerator
     * @return
     */
    private static String tryGetAccessorClassName(String className, ClassNameGenerator classNameGenerator) {
        String accessorName = classNameGenerator.generateInnerAccessorName(className);
//        Set<Class<?>> loadedClass = InstrumentationUtil.findLoadedClass(Environment.getInst(), accessorName);
//        if (CollectionUtil.isNotEmpty(loadedClass)) {
//            return accessorName;
//        }
        if (CompatibilityModeClassLoader.isLoaded(accessorName)) {
            return accessorName;
        }
        return null;
    }

    private static Set<AsmMethod> collectAccessibleMethods(ClazzDefinition definition, boolean containSuper) {
        Set<AsmMethod> accessible = new HashSet<>();
        // my all method
        accessible.addAll(definition.getAsmMethods());
        // non private method in super class

        // default method with interface class

        return accessible;
    }

    private static Set<FieldDesc> collectAccessibleFields(ClazzDefinition definition, boolean containSuper) {
        // my all field

        // non private field in super class

        return null;
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
