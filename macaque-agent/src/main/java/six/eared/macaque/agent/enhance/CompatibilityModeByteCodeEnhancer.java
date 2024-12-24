package six.eared.macaque.agent.enhance;

import io.github.hhy50.linker.asm.AsmClassBuilder;
import io.github.hhy50.linker.asm.MethodBuilder;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import six.eared.macaque.agent.accessor.Accessor;
import six.eared.macaque.agent.accessor.AccessorUtil;
import six.eared.macaque.agent.asm2.AsmClassBuilderExt;
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
import java.util.Iterator;
import java.util.List;


public class CompatibilityModeByteCodeEnhancer {

    public static List<ClassIncrementUpdate> enhance(List<ClazzDataDefinition> definitions) throws Exception {

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
        Accessor accessor = createAccessor(definition.getClassName());
        ClazzDefinition originDefinition = AsmUtil.readOriginClass(definition.getClassName());
        ClassIncrementUpdate incrementUpdate = new ClassIncrementUpdate(definition, originDefinition, accessor);

        AsmUtil.visitClass(definition.getBytecode(), new ClassVisitorDelegation(null) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                AsmMethod asmMethod = definition.getMethod(name, desc);
                MethodUpdateInfo methodUpdateInfo = new MethodUpdateInfo(asmMethod);
                if (!originDefinition.hasMethod(asmMethod)) {
                    MethodBindInfo bindInfo = MethodBindManager
                            .createMethodBindInfo(definition.getClassName(), asmMethod, accessor.getClassName());
                    methodUpdateInfo.setBindInfo(bindInfo);
                    methodUpdateInfo.setVisitorCaller(new BindMethodWriter(accessor));
                } else {
                    methodUpdateInfo.setVisitorCaller(new AsmMethodVisitorCaller());
                }
                incrementUpdate.addMethod(methodUpdateInfo);
                return methodUpdateInfo.getVisitorCaller();
            }

            @Override
            public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
                AsmField asmField = definition.getField(name, desc);
                FieldUpdateInfo fieldUpdateInfo = new FieldUpdateInfo(asmField);
                incrementUpdate.addField(fieldUpdateInfo);
                return null;
            }
        });
        return incrementUpdate;
    }


    private static void bytecodeConvert(ClassIncrementUpdate classUpdateInfo) throws EnhanceException {
        generateNewByteCode(classUpdateInfo);

        if (CollectionUtil.isNotEmpty(classUpdateInfo.getMethods())) {
            Iterator<MethodUpdateInfo> iterator = classUpdateInfo.getMethods().iterator();
            while (iterator.hasNext()) {
                MethodUpdateInfo newMethod = iterator.next();
                MethodBindInfo bindInfo = newMethod.getBindInfo();
                if (bindInfo == null) {
                    throw new EnhanceException("not method bind info");
                }

                BindMethodWriter bindMethodWriter = (BindMethodWriter) newMethod.getVisitorCaller();
                AsmClassBuilder classBuilder = new AsmClassBuilder(Opcodes.ACC_PUBLIC, bindInfo.getBindClass(), null, null, null)
                        .defineMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                                bindInfo.getBindMethod(), bindInfo.getBindMethodDesc(),
                                newMethod.getExceptions())
                        .acceptWithEnd(body -> bindMethodWriter.accept(body.getWriter()));

                ClazzDataDefinition bindClazzDefinition = AsmClassBuilderExt.toDefinition(classBuilder);
                if (bindInfo.isLoaded()) {
                    classUpdateInfo.addCorrelationClasses(CorrelationEnum.METHOD_BIND, bindClazzDefinition);
                } else {
                    EnhanceBytecodeClassLoader.loadClass(bindInfo.getBindClass(), bindClazzDefinition.getBytecode());
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
        AsmClassBuilder classBuilder = new AsmClassBuilder(originClass.getModifiers() | Opcodes.ACC_OPEN,
                originClass.getClassName(),
                originClass.getSuperClassName(),
                originClass.getInterfaces(),
                originClass.getSign());
        for (AsmField field : originClass.getAsmFields()) {
            classBuilder.defineField(field.getModifier(), field.getFieldName(), Type.getType(field.getDesc()), field.getFieldSign(), field.getValue());
            FieldUpdateInfo fi = classIncrementUpdate.getField(field.getFieldName(), field.getDesc());
            classIncrementUpdate.remove(fi);
        }
        for (AsmMethod method : originClass.getAsmMethods()) {
            MethodBuilder methodBuilder = classBuilder.defineMethod(method.getModifier(), method.getMethodName(), method.getDesc(), method.getExceptions());
            MethodVisitor methodWrite = methodBuilder.getMethodBody().getWriter();
            MethodUpdateInfo mi = classIncrementUpdate.getMethod(method.getMethodName(), method.getDesc());
            if (mi == null || mi.getAsmMethod().isStatic() ^ method.isStatic()) {
                if (method.isClinit() || method.isConstructor()) {
                    AsmUtil.areturn(methodWrite, Type.getMethodType(method.getDesc()).getReturnType());
                } else {
                    // 将类上面需要删除的方法， 删掉
                    AsmUtil.throwNoSuchMethod(methodWrite, method.getMethodName());
                    methodBuilder.getMethodBody().end();
                }
                continue;
            } else if (mi.getAsmMethod().isStatic() ^ method.isStatic()) {
                continue;
            }
            AsmMethodVisitorCaller visitorCaller = (AsmMethodVisitorCaller) mi.getVisitorCaller();
            if (visitorCaller == null || visitorCaller.isEmpty()) {
                throw new ByteCodeConvertException("no bytecode found");
            }
            visitorCaller.accept(new InvokeCodeConvertor(method, methodWrite));
            classIncrementUpdate.remove(mi);
        }
        classIncrementUpdate.setEnhancedByteCode(classBuilder.toBytecode());
        if (Environment.isDebug()) {
            FileUtil.writeBytes(new File(FileUtil.getProcessTmpPath()+"/compatibility/"+ClassUtil.toSimpleName(classIncrementUpdate.getClassName())+".class"),
                    classIncrementUpdate.getEnhancedByteCode());
        }
    }

    /**
     * 创建访问器
     *
     * @param className
     */
    private static Accessor createAccessor(String className) {
        // 计算深度
        int deepth = 3;
        Accessor accessor = AccessorUtil.createAccessor(className, new AccessorClassNameGenerator(), deepth);
        return accessor;
    }
}
