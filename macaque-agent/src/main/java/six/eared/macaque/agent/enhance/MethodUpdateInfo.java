package six.eared.macaque.agent.enhance;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.objectweb.asm.tree.MethodNode;
import six.eared.macaque.agent.asm2.AsmMethod;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MethodUpdateInfo {
    private AsmMethod asmMethod;
    private MethodBindInfo bindInfo;
    private MethodNode visitorCaller;

    public MethodUpdateInfo(AsmMethod asmMethod) {
        this.asmMethod = asmMethod;
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
