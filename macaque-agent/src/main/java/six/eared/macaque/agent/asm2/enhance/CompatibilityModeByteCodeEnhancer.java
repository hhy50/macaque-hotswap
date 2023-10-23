package six.eared.macaque.agent.asm2.enhance;

import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.vcs.VersionChainTool;

import java.io.IOException;

public class CompatibilityModeByteCodeEnhancer {

    private static final ClassNameGenerator CLASS_NAME_GENERATOR = new SimpleClassNameGenerator();

    public static void enhance(ClazzDefinition definition) throws IOException, ClassNotFoundException {
        ClazzDefinition accessor = createAccessor(definition);

        // 准备阶段
        prepare(definition, accessor);

        // 转换阶段
        bytecodeConvert(definition, accessor);
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

    private static void prepare(ClazzDefinition definition, ClazzDefinition assessorDefinition) throws IOException, ClassNotFoundException {
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
                if (method.isConstructor() || method.isClinit()) {
                    continue;
                }

                // 跳过非私有方法
                if (!method.isPrivate()) {
                    continue;
                }

                if (method.getMethodBindInfo() != null) {
                    asmMethod.setMethodBindInfo(method.getMethodBindInfo().clone());
                    continue;
                }
            }

            // 私有方法或者新方法。需要建立绑定关系
            String bindMethodName = asmMethod.getMethodName();
            String bindClassName = CLASS_NAME_GENERATOR.generate(definition.getClassName(), bindMethodName);

            MethodBindInfo methodBindInfo = new MethodBindInfo();
            methodBindInfo.setBindClass(bindClassName);
            methodBindInfo.setBindMethod(bindMethodName);
            methodBindInfo.setAccessorClassName(assessorDefinition.getClassName());
            asmMethod.setMethodBindInfo(methodBindInfo);
        }
    }

    private static void bytecodeConvert(ClazzDefinition definition, ClazzDefinition accessor) {

    }
}
