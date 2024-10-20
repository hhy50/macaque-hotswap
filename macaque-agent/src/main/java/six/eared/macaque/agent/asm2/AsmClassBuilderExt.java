package six.eared.macaque.agent.asm2;

import io.github.hhy50.linker.asm.AsmClassBuilder;
import six.eared.macaque.agent.enhance.ClazzDataDefinition;

public class AsmClassBuilderExt {
    public static ClazzDataDefinition toDefinition(AsmClassBuilder classBuilder) {
        return AsmUtil.readClass(classBuilder.toBytecode());
    }
}
