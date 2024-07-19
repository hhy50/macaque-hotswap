package six.eared.macaque.agent.enhance;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.classes.AsmMethodVisitorCaller;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MethodInstance {
    private AsmMethod asmMethod;
    private MethodBindInfo bindInfo;
    private AsmMethodVisitorCaller visitorCaller;

    public MethodInstance(AsmMethod asmMethod, AsmMethodVisitorCaller visitorCaller) {
        this.asmMethod = asmMethod;
        this.visitorCaller = visitorCaller;
    }

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
