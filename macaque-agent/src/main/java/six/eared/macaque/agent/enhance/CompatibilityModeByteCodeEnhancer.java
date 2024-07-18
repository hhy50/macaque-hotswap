package six.eared.macaque.agent.enhance;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import six.eared.macaque.agent.accessor.CompatibilityModeAccessorUtilV2;
import six.eared.macaque.agent.asm2.AsmClassBuilder;
import six.eared.macaque.agent.asm2.AsmField;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.classes.AsmMethodVisitorCaller;
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

    private static ClassIncrementUpdate prepare(ClazzDataDefinition definition) {
        ClazzDefinition accessor = createAccessor(definition.getClassName());
        ClassIncrementUpdate incrementUpdate = new ClassIncrementUpdate(definition, accessor);

        ClassNode classNode = new ClassNode();
        AsmUtil.visitClass(definition.getBytecode(), classNode);

        for (MethodNode method : classNode.methods) {
            MethodInstance methodInstance = new MethodInstance();
            incrementUpdate.addMethod(methodInstance);
        }

        return incrementUpdate;
    }


    private static void bytecodeConvert(ClassIncrementUpdate classUpdateInfo) throws EnhanceException {
        generateNewByteCode(classUpdateInfo);
        if (CollectionUtil.isNotEmpty(classUpdateInfo.getMethods())) {
            for (MethodInstance newMethod : classUpdateInfo.getMethods()) {
                MethodBindInfo bindInfo = newMethod.getMethodBindInfo();
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
            }
        }
    }

    /**
     * 生成新的字节码
     */
    private static void generateNewByteCode(ClassIncrementUpdate classIncrementUpdate) throws ByteCodeConvertException {
        ClazzDefinition originClass = null;
        try {
            originClass = AsmUtil.readOriginClass(classIncrementUpdate.getClassName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ClassWriter classWriter = new ClassWriter(0);
        for (AsmField field : originClass.getAsmFields()) {
            classWriter.visitField(field.getModifier(), field.getFieldName(), field.getDesc(),
                    field.getFieldSign(), field.getValue());
        }

        for (AsmMethod method : originClass.getAsmMethods()) {
            MethodVisitor methodWrite = classWriter.visitMethod(method.getModifier(), method.getMethodName(), method.getDesc(),
                    method.getMethodSign(), method.getExceptions());
            MethodInstance mi = classIncrementUpdate.getMethod(method.getMethodName(), method.getDesc());
            if (mi == null) {
                // 将类上面需要删除的方法， 删掉
                int lvblen = AsmUtil.calculateLvbOffset(method.isStatic(), Type.getArgumentTypes(method.getDesc()));
                methodWrite.visitMaxs(3, lvblen);
                AsmUtil.throwNoSuchMethod(methodWrite, method.getMethodName());
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
