package six.eared.macaque.agent.enhance;

import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.ClassMethodUniqueDesc;

import java.util.HashMap;
import java.util.Map;

public class MethodBindManager {

    private static final BindMethodNameGenerator CLASS_NAME_GENERATOR = new BindMethodNameGenerator();

    /**
     * 只有新方法才有bind info
     */
    private static final Map<ClassMethodUniqueDesc, MethodBindInfo> BIND_INFO_MAP = new HashMap<>();

    public static MethodBindInfo createMethodBindInfo(String clazzName, AsmMethod method, String accessorName) {
        MethodBindInfo bindInfo = getBindInfo(clazzName, method.getMethodName(), method.getDesc(), method.isStatic());
        if (bindInfo != null) {
            return bindInfo;
        }

        String bindMethodName = method.getMethodName();
        String bindClassName = CLASS_NAME_GENERATOR.generate(clazzName, bindMethodName);

        MethodBindInfo methodBindInfo = new MethodBindInfo();
        methodBindInfo.setBindClass(bindClassName);
        methodBindInfo.setBindMethod("invoke");
        methodBindInfo.setBindMethodDesc(method.isStatic()?method.getDesc():AsmUtil.addArgsDesc(method.getDesc(), accessorName, true));
        methodBindInfo.setStatic(method.isStatic());
        methodBindInfo.setAccessorClass(accessorName);

        putBindInfo(clazzName, bindMethodName, method.getDesc(), method.isStatic(), methodBindInfo);
        return methodBindInfo;
    }

    public static MethodBindInfo createPatchedBindInfo(String clazzName, AsmMethod method, String accessorName) {
        MethodBindInfo bindInfo = getBindInfo(clazzName, method.getMethodName(), method.getDesc(), method.isStatic());
        if (bindInfo != null) {
            return bindInfo;
        }

        String bindMethodName = method.getMethodName();
        String bindClassName = CLASS_NAME_GENERATOR.generate(clazzName, bindMethodName);

        MethodBindInfo methodBindInfo = new MethodBindInfo();
        methodBindInfo.setBindClass(bindClassName);
        methodBindInfo.setBindMethod("invoke");
        methodBindInfo.setBindMethodDesc(method.isStatic()?method.getDesc():AsmUtil.addArgsDesc(method.getDesc(), accessorName, true));
        methodBindInfo.setStatic(method.isStatic());
        methodBindInfo.setAccessorClass(accessorName);

        putBindInfo(clazzName, bindMethodName, method.getDesc(), method.isStatic(), methodBindInfo);
        return methodBindInfo;
    }

    private static void putBindInfo(String clazzName, String methodName, String desc, boolean isStatic, MethodBindInfo methodBindInfo) {
        if (isStatic) methodName = "static#"+methodName;
        BIND_INFO_MAP.put(ClassMethodUniqueDesc.of(clazzName, methodName, desc), methodBindInfo);
    }

    public static MethodBindInfo getBindInfo(String clazzName, String methodName, String desc, boolean isStatic) {
        if (isStatic) methodName = "static#"+methodName;
        return BIND_INFO_MAP.get(ClassMethodUniqueDesc.of(clazzName, methodName, desc));
    }
}
