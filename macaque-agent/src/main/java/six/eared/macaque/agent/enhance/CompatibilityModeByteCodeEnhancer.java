package six.eared.macaque.agent.enhance;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import six.eared.macaque.agent.accessor.CompatibilityModeAccessorUtil;
import six.eared.macaque.agent.asm2.AsmClassBuilder;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.ClassIncrementUpdate;
import six.eared.macaque.agent.asm2.classes.AsmMethodVisitorCaller;
import six.eared.macaque.agent.asm2.classes.ClassVisitorDelegation;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.enums.CorrelationEnum;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.agent.exceptions.EnhanceException;
import six.eared.macaque.agent.vcs.VersionChainTool;
import six.eared.macaque.asm.ClassWriter;
import six.eared.macaque.asm.MethodVisitor;
import six.eared.macaque.asm.Opcodes;
import six.eared.macaque.asm.Type;
import six.eared.macaque.common.util.ClassUtil;
import six.eared.macaque.common.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class CompatibilityModeByteCodeEnhancer {

    private static final ClassNameGenerator CLASS_NAME_GENERATOR = new SimpleClassNameGenerator();

    public static Map<String, byte[]> enhance(List<ClazzDefinition> definitions) throws IOException, ClassNotFoundException,
            NotFoundException, CannotCompileException {

        List<ClassIncrementUpdate> classIncrementUpdates = new ArrayList<>();
        for (ClazzDefinition definition : definitions) {
            // 准备
            ClassIncrementUpdate incrementUpdate = prepare(definition);
            if (incrementUpdate != null)
                classIncrementUpdates.add(incrementUpdate);
        }
        for (ClassIncrementUpdate updateInfo : classIncrementUpdates) {
            // 转换
            bytecodeConvert(updateInfo);
        }
    }

    private static ClassIncrementUpdate prepare(ClazzDefinition definition) throws IOException, ClassNotFoundException {
        ClazzDefinition accessor = createAccessor(definition.getClassName());

        ClazzDefinition originClass = AsmUtil.readOriginClass(definition.getClassName());
        ClazzDefinition lastVersionClass = VersionChainTool.findLastClassVersion(definition.getClassName(), false);
        if (lastVersionClass == null) {
            lastVersionClass = originClass;
        }

        ClassIncrementUpdate incrementUpdate = new ClassIncrementUpdate(definition);
        for (AsmMethod asmMethod : originClass.getAsmMethods()) {
            if (asmMethod.isConstructor() || asmMethod.isClinit()) continue;
            AsmMethod method = definition.getMethod(asmMethod.getMethodName(), asmMethod.getDesc());
            if (method == null || method.isStatic() ^ asmMethod.isStatic()) {
                incrementUpdate.addDeleted(asmMethod);
            }
        }
        for (AsmMethod asmMethod : definition.getAsmMethods()) {
            if (asmMethod.isConstructor() || asmMethod.isClinit()) continue;
            AsmMethod method = lastVersionClass.getMethod(asmMethod.getMethodName(), asmMethod.getDesc());
            if (method == null || method.isStatic() ^ asmMethod.isStatic()) {
                // 跳过构造函数和clinit
                if (method.getMethodBindInfo() != null) {
                    asmMethod.setMethodBindInfo(method.getMethodBindInfo().clone());
                }
                // 如果添加/删除了 static修饰
                if (asmMethod.isStatic() ^ method.isStatic()) {
                    // to bind
                } else continue;
            }
            asmMethod.setMethodBindInfo(buildBindInfo(definition.getClassName(), asmMethod, accessor.getClassName()));
        }

        for (AsmMethod asmMethod : lastClassVersion.getAsmMethods()) {
            AsmMethod method = definition.getMethod(asmMethod.getMethodName(), asmMethod.getDesc());
            // 删除的方法
            if (method == null) {
                asmMethod.setDeleted(true);
                definition.addAsmMethod(asmMethod);
            } else if (asmMethod.isStatic() ^ method.isStatic()) {
                asmMethod.setDeleted(true);
                definition.addAsmMethod(asmMethod);
            }
        }
    }

    private static Map<String, byte[]> bytecodeConvert(ClassIncrementUpdate classUpdateInfo) {
        byte[] newByteCode = generateNewByteCode(classUpdateInfo);

        for (AsmMethod method : definition.getAsmMethods()) {
            if (method.getMethodBindInfo() == null) continue;
            MethodBindInfo bindInfo = method.getMethodBindInfo();
            AsmMethodVisitorCaller visitorCaller = bindInfo.getVisitorCaller();
            if (visitorCaller == null || visitorCaller.isEmpty()) {
                throw new EnhanceException("read new method error");
            }

            AsmClassBuilder classBuilder = AsmUtil.defineClass(Opcodes.ACC_PUBLIC, bindInfo.getBindClass(), null, null, null)
                    .defineMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                            bindInfo.getBindMethod(), bindInfo.getBindMethodDesc(),
                            method.getExceptions(), method.getMethodSign())
                    .accept(writer -> visitorCaller.accept(new BindMethodWriter(writer, method, bindInfo)))
                    .end();
            ClazzDefinition bindClazzDefinition = classBuilder.toDefinition();
            if (bindInfo.isLoaded()) {
                definition.addCorrelationClasses(CorrelationEnum.METHOD_BIND, bindClazzDefinition);
            } else {
                CompatibilityModeClassLoader.loadClass(bindInfo.getBindClass(), bindClazzDefinition.getByteArray());
                bindInfo.setLoaded(true);
            }
        }
        if (Environment.isDebug()) {
            FileUtil.writeBytes(
                    new File(FileUtil.getProcessTmpPath() + File.separator + ClassUtil.toSimpleName(definition.getClassName()) + ".class"),
                    newByteCode);
        }
        definition.setByteCode(newByteCode);
        return definition;
    }

    /**
     * 生成新的字节码
     */
    private static byte[] generateNewByteCode(ClazzDefinition definition) {
        Map<String, MethodBindInfo> bindMethods = definition.getAsmMethods().stream()
                .filter(item -> item.getMethodBindInfo() != null)
                .collect(Collectors.toMap(AsmMethod::getUniqueDesc, AsmMethod::getMethodBindInfo));

        ClassWriter classWriter = new ClassWriter(0);
        definition.revisit(new ClassVisitorDelegation(classWriter) {
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                super.visit(version, access, name, signature, superName, interfaces);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                AsmMethod method = definition.getMethod(name, desc);
                MethodBindInfo bindInfo = method.getMethodBindInfo();
                if (bindInfo == null) {
                    MethodVisitor writer = super.visitMethod(access, name, desc, signature, exceptions);
                    return new InvokeCodeConvertor(writer, bindMethods);
                }
                return bindInfo.getVisitorCaller().createProxyObj();
            }

            @Override
            public void visitEnd() {
                for (AsmMethod method : definition.getAsmMethods()) {
                    // 将类上面需要删除的方法， 删掉
                    if (!method.isDeleted() || method.getMethodBindInfo() != null) continue;
                    MethodVisitor methodWrite = super.visitMethod(method.getModifier(), method.getMethodName(), method.getDesc(),
                            method.getMethodSign(), method.getExceptions());
                    int lvblen = AsmUtil.calculateLvbOffset(method.isStatic(), Type.getArgumentTypes(method.getDesc()));
                    methodWrite.visitMaxs(lvblen + 3, lvblen);
                    AsmUtil.throwNoSuchMethod(methodWrite, method.getMethodName());
                }
                super.visitEnd();
            }
        });
        return classWriter.toByteArray();
    }

    /**
     * 创建访问器
     *
     * @param definition
     */
    private static ClazzDefinition createAccessor(String className) {
        // 计算深度
        int deepth = 5;
        ClazzDefinition accessor = CompatibilityModeAccessorUtil.createAccessor(className, CLASS_NAME_GENERATOR, deepth);
        return accessor;
    }

    public static MethodBindInfo buildBindInfo(String clazzName, AsmMethod method, String accessorName) {
        String bindMethodName = method.getMethodName();
        String bindClassName = CLASS_NAME_GENERATOR.generate(clazzName, bindMethodName);

        MethodBindInfo methodBindInfo = new MethodBindInfo();
        methodBindInfo.setBindClass(bindClassName);
        methodBindInfo.setBindMethod(bindMethodName);
        methodBindInfo.setBindMethodDesc(AsmUtil.addArgsDesc(method.getDesc(), accessorName, !method.isStatic()));
        methodBindInfo.setAccessorClass(accessorName);
        methodBindInfo.setVisitorCaller(new AsmMethodVisitorCaller());
        return methodBindInfo;
    }
}
