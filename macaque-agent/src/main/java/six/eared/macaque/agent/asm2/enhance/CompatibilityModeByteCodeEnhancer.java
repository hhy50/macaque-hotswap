package six.eared.macaque.agent.asm2.enhance;

import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.ClassBuilder;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.vcs.VersionChainTool;
import six.eared.macaque.asm.Opcodes;
import six.eared.macaque.asm.Type;

import java.io.IOException;
import java.util.List;

import static six.eared.macaque.agent.asm2.AsmUtil.areturn;

public class CompatibilityModeByteCodeEnhancer {

    private static final ClassNameGenerator CLASS_NAME_GENERATOR = new SimpleClassNameGenerator();

    public static void enhance(List<ClazzDefinition> definitions) throws IOException, ClassNotFoundException {
        for (ClazzDefinition definition : definitions) {
            // 准备
            prepare(definition);
        }
        for (ClazzDefinition definition : definitions) {
            // 转换
            bytecodeConvert(definition);
        }
    }

    private static void prepare(ClazzDefinition definition) throws IOException, ClassNotFoundException {
        ClazzDefinition accessor = createAccessor(AsmUtil.readOriginClass(definition.getClassName()));

        ClazzDefinition lastClassVersion = VersionChainTool.findLastClassVersion(definition.getClassName(), false);
        if (lastClassVersion == null) {
            lastClassVersion = AsmUtil.readOriginClass(definition.getClassName());
        }
        assert lastClassVersion != null;
        for (AsmMethod asmMethod : definition.getAsmMethods()) {
            // 已存在的方法
            if (lastClassVersion.hasMethod(asmMethod.getMethodName(), asmMethod.getDesc())) {
                AsmMethod method = lastClassVersion.getMethod(asmMethod.getMethodName(), asmMethod.getDesc());
                // 跳过构造函数和clinit
                if (method.isConstructor() || method.isClinit()) continue;
                // 跳过非私有方法
                if (!method.isPrivate()) continue;
                if (method.getMethodBindInfo() != null) {
                    asmMethod.setMethodBindInfo(method.getMethodBindInfo().clone());
                    continue;
                }
            }

            // 私有方法或者新方法。需要建立绑定关系
            String bindMethodName = asmMethod.getMethodName();
            String bindClassName = CLASS_NAME_GENERATOR.generate(definition.getClassName(), bindMethodName);

            // 生成bind class和bind method
            ClassBuilder classBuilder = AsmUtil.defineClass(Opcodes.ACC_PUBLIC, bindClassName, null, null, null)
                    .defineMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, bindMethodName,
                            AsmUtil.addArgsDesc( asmMethod.getDesc(), accessor.getClassName(),true),
                            asmMethod.getExceptions(), asmMethod.getMethodSign())
                    .accept(method -> {
                        Type methodType = Type.getMethodType(asmMethod.getDesc());

                        method.visitMaxs(1, 1);
                        method.visitInsn(1);

                        // return;
                        areturn(method, methodType.getReturnType());
                    })
                    .end();
            CompatibilityModeClassLoader.loadClass(bindClassName, classBuilder.toByteArray());

            MethodBindInfo methodBindInfo = new MethodBindInfo();
            methodBindInfo.setBindClass(bindClassName);
            methodBindInfo.setBindMethod(bindMethodName);

            asmMethod.setMethodBindInfo(methodBindInfo);
        }
    }

    private static void bytecodeConvert(ClazzDefinition definition) {

    }

    /**
     * 创建访问器
     *
     * @param definition
     */
    private static ClazzDefinition createAccessor(ClazzDefinition definition) {
        // 计算深度
        int depth = 3;
        ClazzDefinition accessor = CompatibilityModeAccessorUtil.createAccessor(definition.getClassName(), CLASS_NAME_GENERATOR, depth);
        return accessor;
    }
}
