package six.eared.macaque.agent.enhance;

import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.classes.AsmMethodVisitorCaller;

import java.util.HashMap;
import java.util.Map;

public class MethodBindManager {

    private static final BindMethodNameGenerator CLASS_NAME_GENERATOR = new BindMethodNameGenerator();

    /**
     * 只有新方法才有bind info
     */
    private static final Map<String, MethodBindInfo> BIND_INFO_MAP = new HashMap<>();

    public static MethodBindInfo createMethodBindInfo(String clazzName, AsmMethod method, String accessorName) {
        MethodBindInfo bindInfo = getBindInfo(clazzName, method.getMethodName(), method.getDesc(), method.isStatic());
        if (bindInfo != null) {
            return bindInfo;
        }

        String bindMethodName = method.getMethodName();
        String bindClassName = CLASS_NAME_GENERATOR.generate(clazzName, bindMethodName);

        MethodBindInfo methodBindInfo = new MethodBindInfo();
        methodBindInfo.setBindClass(bindClassName);
        methodBindInfo.setBindMethod(bindMethodName);
        methodBindInfo.setBindMethodDesc(AsmUtil.addArgsDesc(method.getDesc(), accessorName, !method.isStatic()));
        methodBindInfo.setAccessorClass(accessorName);
        methodBindInfo.setVisitorCaller(new AsmMethodVisitorCaller());

        putBindInfo(clazzName, bindMethodName, method.getDesc(), method.isStatic(), methodBindInfo);
        return methodBindInfo;
    }

    private static void putBindInfo(String clazzName, String method, String desc, boolean isStatic, MethodBindInfo methodBindInfo) {
        if (isStatic) method = "static#"+method;
        BIND_INFO_MAP.put(clazzName + "#" + method + "#" + desc, methodBindInfo);
    }

    public static MethodBindInfo getBindInfo(String clazzName, String method, String desc, boolean isStatic) {
        if (isStatic) method = "static#"+method;
        return BIND_INFO_MAP.get(clazzName + "#" + method + "#" + desc);
    }
}
