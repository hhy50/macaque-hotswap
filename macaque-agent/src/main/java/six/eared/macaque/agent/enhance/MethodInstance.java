package six.eared.macaque.agent.enhance;


import lombok.Data;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.classes.AsmMethodVisitorCaller;

@Data
public class MethodInstance {
    private AsmMethod asmMethod;
    private MethodBindInfo methodBindInfo;
    private AsmMethodVisitorCaller visitorCaller;

    public String getMethodName() {
        return this.asmMethod.getMethodName();
    }

    public String getDesc() {
        return this.asmMethod.getDesc();
    }

    public String[] getExceptions() {
        return this.asmMethod.getExceptions();
    }

    public String getMethodSign() {
        return this.asmMethod.getMethodSign();
    }
}
