package six.eared.macaque.agent.asm2.classes;

import six.eared.macaque.agent.asm2.AsmMethod;

public class AsmMethodHolder {

    private AsmMethod asmMethod;

    private AsmMethodVisitorCaller caller;


    public AsmMethod getAsmMethod() {
        return asmMethod;
    }

    public AsmMethodVisitorCaller getCaller() {
        return caller;
    }

    public void setAsmMethod(AsmMethod asmMethod) {
        this.asmMethod = asmMethod;
    }

    public void setCaller(AsmMethodVisitorCaller caller) {
        this.caller = caller;
    }
}
