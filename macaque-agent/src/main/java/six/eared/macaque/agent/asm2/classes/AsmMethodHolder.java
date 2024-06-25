package six.eared.macaque.agent.asm2.classes;

import lombok.Data;
import six.eared.macaque.agent.asm2.AsmMethod;


@Data
public class AsmMethodHolder {

    private AsmMethod asmMethod;

    private AsmMethodVisitorCaller caller;
}
