package six.eared.macaque.agent.asm2.enhance;

import six.eared.macaque.common.util.ClassUtil;

public class SimpleClassNameGenerator implements ClassNameGenerator {


    @Override
    public String generate(String className, String methodName) {
        return ClassUtil.simpleClassName2path(className + "$$Macaque_" + methodName);
    }
}
