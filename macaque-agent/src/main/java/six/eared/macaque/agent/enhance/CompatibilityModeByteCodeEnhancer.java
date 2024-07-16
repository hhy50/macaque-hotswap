package six.eared.macaque.agent.enhance;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
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
import six.eared.macaque.agent.exceptions.EnhanceException;
import six.eared.macaque.agent.vcs.VersionChainTool;
import six.eared.macaque.common.util.ClassUtil;
import six.eared.macaque.common.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class CompatibilityModeByteCodeEnhancer {


    public static List<ClassIncrementUpdate> enhance(List<ClazzDefinition> definitions) throws IOException, ClassNotFoundException,
            NotFoundException, CannotCompileException {

        List<ClassIncrementUpdate> changedClass = new ArrayList<>();
        for (ClazzDefinition definition : definitions) {
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

    private static ClassIncrementUpdate prepare(ClazzDefinition definition) throws IOException, ClassNotFoundException {
        ClazzDefinition accessor = createAccessor(definition.getClassName());

        ClassIncrementUpdate incrementUpdate = new ClassIncrementUpdate(definition);
        ClazzDefinition originClass = AsmUtil.readOriginClass(definition.getClassName());

        for (AsmMethod asmMethod : originClass.getAsmMethods()) {
            AsmMethod method = definition.getMethod(asmMethod.getMethodName(), asmMethod.getDesc());
            if (asmMethod.isConstructor() || asmMethod.isClinit()) continue;
            if (method == null || method.isStatic() ^ asmMethod.isStatic()) {
                incrementUpdate.addDeletedMethod(asmMethod);
            }
        }
        for (AsmField asmField : originClass.getAsmFields()) {
            AsmField field = definition.getField(asmField.getFieldName(), asmField.getDesc());
            if (field == null || field.isStatic() ^ asmField.isStatic()) {
                incrementUpdate.addDeletedField(asmField);
            }
        }

        for (AsmMethod asmMethod : definition.getAsmMethods()) {
            if (asmMethod.isConstructor() || asmMethod.isClinit()) continue;
            AsmMethod method = originClass.getMethod(asmMethod.getMethodName(), asmMethod.getDesc());
            if (method == null || method.isStatic() ^ asmMethod.isStatic()) {
                MethodBindInfo bindInfo = MethodBindManager
                        .createMethodBindInfo(definition.getClassName(), asmMethod, accessor.getClassName());
                asmMethod.setBindInfo(bindInfo);
                incrementUpdate.addNewMethod(asmMethod);
            }
        }

        ClazzDefinition lastClassVersion = VersionChainTool.findLastClassVersion(definition.getClassName(), false);
        if (lastClassVersion != null) {
            for (AsmMethod asmMethod : lastClassVersion.getAsmMethods()) {
                if (asmMethod.isConstructor() || asmMethod.isClinit()
                        || asmMethod.getBindInfo() == null) continue;
                AsmMethod method = definition.getMethod(asmMethod.getMethodName(), asmMethod.getDesc());
                if (method == null || method.isStatic() ^ asmMethod.isStatic()) {
                    incrementUpdate.addDeletedMethod(asmMethod);
                }
            }
        }
        return incrementUpdate;
    }


    private static void bytecodeConvert(ClassIncrementUpdate classUpdateInfo) throws EnhanceException {
        byte[] newByteCode = generateNewByteCode(classUpdateInfo);
        classUpdateInfo.setEnhancedByteCode(newByteCode);
        if (classUpdateInfo.getNewMethods() == null) {
            return;
        }
        for (AsmMethod newMethod : classUpdateInfo.getNewMethods()) {
            MethodBindInfo bindInfo = newMethod.getBindInfo();
            AsmMethodVisitorCaller visitorCaller = bindInfo.getVisitorCaller();
            if (visitorCaller == null || visitorCaller.isEmpty()) {
                throw new EnhanceException("read new method error");
            }
            AsmClassBuilder classBuilder = AsmUtil.defineClass(Opcodes.ACC_PUBLIC, bindInfo.getBindClass(), null, null, null)
                    .defineMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                            bindInfo.getBindMethod(), bindInfo.getBindMethodDesc(),
                            newMethod.getExceptions(), newMethod.getMethodSign())
                    .accept(writer -> visitorCaller.accept(new BindMethodWriter(writer, newMethod, bindInfo)))
                    .end();
            ClazzDefinition bindClazzDefinition = classBuilder.toDefinition();
            if (bindInfo.isLoaded()) {
                classUpdateInfo.addCorrelationClasses(CorrelationEnum.METHOD_BIND, bindClazzDefinition);
            } else {
                CompatibilityModeClassLoader.loadClass(bindInfo.getBindClass(), bindClazzDefinition.getByteArray());
                bindInfo.setLoaded(true);
            }
        }
        if (Environment.isDebug()) {
            FileUtil.writeBytes(new File(FileUtil.getProcessTmpPath() + "/" + ClassUtil.toSimpleName(classUpdateInfo.getClassName()) + ".class"),
                    classUpdateInfo.getEnhancedByteCode());
        }
    }

    /**
     * 生成新的字节码
     */
    private static byte[] generateNewByteCode(ClassIncrementUpdate classIncrementUpdate) {
        ClazzDefinition definition = classIncrementUpdate.getClazzDefinition();
        ClassWriter classWriter = new ClassWriter(0);
        definition.revisit(new ClassVisitorDelegation(classWriter) {
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                super.visit(version, access, name, signature, superName, interfaces);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                AsmMethod method = definition.getMethod(name, desc);
                MethodBindInfo bindInfo = method.getBindInfo();
                if (bindInfo == null) {
                    MethodVisitor writer = super.visitMethod(access, name, desc, signature, exceptions);
                    return new InvokeCodeConvertor(method, writer);
                }
                return bindInfo.getVisitorCaller().createProxyObj();
            }

            @Override
            public void visitEnd() {
                if (classIncrementUpdate.getDeletedMethods() != null) {
                    for (AsmMethod method : classIncrementUpdate.getDeletedMethods()) {
                        if (method.getBindInfo() != null) continue;

                        // 将类上面需要删除的方法， 删掉
                        MethodVisitor methodWrite = super.visitMethod(method.getModifier(), method.getMethodName(), method.getDesc(),
                                method.getMethodSign(), method.getExceptions());
                        int lvblen = AsmUtil.calculateLvbOffset(method.isStatic(), Type.getArgumentTypes(method.getDesc()));
                        methodWrite.visitMaxs(3, lvblen);
                        AsmUtil.throwNoSuchMethod(methodWrite, method.getMethodName());
                    }
                }
                if (classIncrementUpdate.getDeletedFields() != null) {
                    for (AsmField field : classIncrementUpdate.getDeletedFields()) {
//                        if (field.getBindInfo() != null) continue;
                       this.visitField(field.getModifier(), field.getFieldName(), field.getDesc(), field.getFieldSign(), field.getValue());
                    }
                }
                super.visitEnd();
            }
        });
        return classWriter.toByteArray();
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
