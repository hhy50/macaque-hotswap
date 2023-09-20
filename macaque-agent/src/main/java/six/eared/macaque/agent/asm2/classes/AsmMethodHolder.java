package six.eared.macaque.agent.asm2.classes;

import six.eared.macaque.agent.asm2.AsmMethod;

public class AsmMethodHolder {

    private AsmMethod asmMethod;

    private MethodVisitorProxy visitor;

    public AsmMethodHolder(AsmMethod asmMethod, MethodVisitorProxy visitor) {
        this.asmMethod = asmMethod;
        this.visitor = visitor;
    }

    public AsmMethod getAsmMethod() {
        return asmMethod;
    }

    public MethodVisitorProxy getVisitor() {
        return visitor;
    }
}
