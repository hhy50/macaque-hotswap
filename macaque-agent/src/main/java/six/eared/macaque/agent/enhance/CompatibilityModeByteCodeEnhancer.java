package six.eared.macaque.agent.enhance;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.objectweb.asm.*;
import six.eared.macaque.agent.accessor.CompatibilityModeAccessorUtilV2;
import six.eared.macaque.agent.asm2.AsmClassBuilder;
import six.eared.macaque.agent.asm2.AsmField;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.classes.AsmMethodVisitorCaller;
import six.eared.macaque.agent.asm2.classes.ClassVisitorDelegation;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.enums.CorrelationEnum;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.agent.exceptions.ByteCodeConvertException;
import six.eared.macaque.agent.exceptions.EnhanceException;
import six.eared.macaque.common.util.ClassUtil;
import six.eared.macaque.common.util.CollectionUtil;
import six.eared.macaque.common.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


public class CompatibilityModeByteCodeEnhancer {

    public static List<ClassIncrementUpdate> enhance(List<ClazzDataDefinition> definitions) throws IOException, ClassNotFoundException,
            NotFoundException, CannotCompileException {

        List<ClassIncrementUpdate> changedClass = new ArrayList<>();
        for (ClazzDataDefinition definition : definitions) {
            // 准备
            ClassIncrementUpdate incrementUpdate = prepare(definition);
            changedClass.add(incrementUpdate);
        }
        for (ClassIncrementUpdate incrementUpdate : changedClass) {
            // 转换
            bytecodeConvert(incrementUpdate);
        }
        return changedClass;
    }

    private static ClassIncrementUpdate prepare(ClazzDataDefinition definition) throws IOException, ClassNotFoundException {
        ClazzDefinition accessor = createAccessor(definition.getClassName());
        ClazzDefinition originDefinition = AsmUtil.readOriginClass(definition.getClassName());
        ClassIncrementUpdate incrementUpdate = new ClassIncrementUpdate(definition, originDefinition, accessor);

        AsmUtil.visitClass(definition.getBytecode(), new ClassVisitorDelegation(null) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                AsmMethod asmMethod = definition.getMethod(name, desc);
                MethodInstance methodInstance = new MethodInstance(asmMethod, new AsmMethodVisitorCaller());
                if (!originDefinition.hasMethod(asmMethod)) {
                    MethodBindInfo bindInfo = MethodBindManager
                            .createMethodBindInfo(definition.getClassName(), asmMethod, accessor.getClassName());
                    methodInstance.setBindInfo(bindInfo);
                }
                incrementUpdate.addMethod(methodInstance);
                return methodInstance.getVisitorCaller();
            }

            @Override
            public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
                AsmField asmField = definition.getField(name, desc);
                FieldInstance fieldInstance = new FieldInstance(asmField);
                incrementUpdate.addField(fieldInstance);
                return null;
            }
        });
        return incrementUpdate;
    }


    private static void bytecodeConvert(ClassIncrementUpdate classUpdateInfo) throws EnhanceException {
        generateNewByteCode(classUpdateInfo);

        if (CollectionUtil.isNotEmpty(classUpdateInfo.getMethods())) {
            Iterator<MethodInstance> iterator = classUpdateInfo.getMethods().iterator();
            while (iterator.hasNext()) {
                MethodInstance newMethod = iterator.next();
                MethodBindInfo bindInfo = newMethod.getBindInfo();
                if (bindInfo == null) {
                    throw new EnhanceException("not method bind info");
                }
                AsmMethodVisitorCaller visitorCaller = newMethod.getVisitorCaller();
                if (visitorCaller == null || visitorCaller.isEmpty()) {
                    throw new EnhanceException("read new method error");
                }
                AsmClassBuilder classBuilder = AsmUtil.defineClass(Opcodes.ACC_PUBLIC, bindInfo.getBindClass(), null, null, null)
                        .defineMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                                bindInfo.getBindMethod(), bindInfo.getBindMethodDesc(),
                                newMethod.getExceptions(), newMethod.getMethodSign())
                        .accept(writer -> visitorCaller.accept(new BindMethodWriter(writer, newMethod.getAsmMethod(), bindInfo)))
                        .end();
                ClazzDataDefinition bindClazzDefinition = classBuilder.toDefinition();
                if (bindInfo.isLoaded()) {
                    classUpdateInfo.addCorrelationClasses(CorrelationEnum.METHOD_BIND, bindClazzDefinition);
                } else {
                    CompatibilityModeClassLoader.loadClass(bindInfo.getBindClass(), bindClazzDefinition.getBytecode());
                    bindInfo.setLoaded(true);
                }
                iterator.remove();
            }
        }
    }

    /**
     * 生成新的字节码
     */
    private static void generateNewByteCode(ClassIncrementUpdate classIncrementUpdate) throws ByteCodeConvertException {
        ClazzDefinition originClass = classIncrementUpdate.getOriginDefinition();

        ClassWriter classWriter = new ClassWriter(0);
        classWriter.visit(originClass.getClassVersion(), originClass.getModifiers() | Opcodes.ACC_OPEN,
                ClassUtil.simpleClassName2path(originClass.getClassName()), originClass.getSign(), ClassUtil.simpleClassName2path(originClass.getSuperClassName()),
                Arrays.stream(originClass.getInterfaces()).map(ClassUtil::simpleClassName2path).toArray(String[]::new));

        // TODO annotation

        for (AsmField field : originClass.getAsmFields()) {
            classWriter.visitField(field.getModifier(), field.getFieldName(), field.getDesc(),
                    field.getFieldSign(), field.getValue());
            FieldInstance fi = classIncrementUpdate.getField(field.getFieldName(), field.getDesc());
            classIncrementUpdate.remove(fi);
        }
        for (AsmMethod method : originClass.getAsmMethods()) {
            MethodVisitor methodWrite = classWriter.visitMethod(method.getModifier(), method.getMethodName(), method.getDesc(),
                    method.getMethodSign(), method.getExceptions());
            MethodInstance mi = classIncrementUpdate.getMethod(method.getMethodName(), method.getDesc());
            if (mi == null || mi.getAsmMethod().isStatic() ^ method.isStatic()) {
                // 将类上面需要删除的方法， 删掉
                int lvblen = AsmUtil.calculateLvbOffset(method.isStatic(), Type.getArgumentTypes(method.getDesc()));
                methodWrite.visitMaxs(3, lvblen);
                AsmUtil.throwNoSuchMethod(methodWrite, method.getMethodName());
                continue;
            } else if (mi.getAsmMethod().isStatic() ^ method.isStatic()) {
                continue;
            }
            AsmMethodVisitorCaller visitorCaller = mi.getVisitorCaller();
            if (visitorCaller == null || visitorCaller.isEmpty()) {
                throw new ByteCodeConvertException("no bytecode found");
            }
            visitorCaller.accept(new InvokeCodeConvertor(method, methodWrite));
            classIncrementUpdate.remove(mi);
        }
        classIncrementUpdate.setEnhancedByteCode(classWriter.toByteArray());
        if (Environment.isDebug()) {
            FileUtil.writeBytes(new File(FileUtil.getProcessTmpPath() + "/" + ClassUtil.toSimpleName(classIncrementUpdate.getClassName()) + ".class"),
                    classIncrementUpdate.getEnhancedByteCode());
        }
    }

    /**
     * 创建访问器
     *
     * @param className
     */
    private static ClazzDefinition createAccessor(String className) {
        // 计算深度
        int deepth = 0;
        ClazzDefinition accessor = CompatibilityModeAccessorUtilV2.createAccessor(className, new AccessorClassNameGenerator(), deepth);
        return accessor;
    }
}
