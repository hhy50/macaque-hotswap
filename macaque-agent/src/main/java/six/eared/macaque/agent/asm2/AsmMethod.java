package six.eared.macaque.agent.asm2;


import six.eared.macaque.agent.asm2.enhance.MethodBindInfo;
import six.eared.macaque.asm.Opcodes;

import java.util.Arrays;
import java.util.Objects;

/**
 *
 */
public class AsmMethod {

    private int modifier;

    private String methodName;

    private String methodSign;

    private String desc;

    private String[] exceptions;

    private MethodBindInfo methodBindInfo;

    public int getModifier() {
        return modifier;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getMethodSign() {
        return methodSign;
    }

    public String getDesc() {
        return desc;
    }

    public String[] getExceptions() {
        return exceptions;
    }

    public MethodBindInfo getMethodBindInfo() {
        return methodBindInfo;
    }

    public void setMethodBindInfo(MethodBindInfo methodBindInfo) {
        this.methodBindInfo = methodBindInfo;
    }

    public boolean isPrivate() {
        return (this.modifier & Opcodes.ACC_PRIVATE) > 0;
    }

    public static final class AsmMethodBuilder {
        private int modifier;
        private String methodName;
        private String methodSign;
        private String desc;
        private String[] exceptions;

        private AsmMethodBuilder() {
        }

        public static AsmMethodBuilder builder() {
            return new AsmMethodBuilder();
        }

        public AsmMethodBuilder modifier(int modifier) {
            this.modifier = modifier;
            return this;
        }

        public AsmMethodBuilder methodName(String methodName) {
            this.methodName = methodName;
            return this;
        }

        public AsmMethodBuilder methodSign(String methodSign) {
            this.methodSign = methodSign;
            return this;
        }

        public AsmMethodBuilder desc(String desc) {
            this.desc = desc;
            return this;
        }

        public AsmMethodBuilder exceptions(String[] exceptions) {
            this.exceptions = exceptions;
            return this;
        }

        public AsmMethod build() {
            AsmMethod asmMethod = new AsmMethod();
            asmMethod.modifier = this.modifier;
            asmMethod.methodName = this.methodName;
            asmMethod.methodSign = this.methodSign;
            asmMethod.desc = this.desc;
            asmMethod.exceptions = this.exceptions;
            return asmMethod;
        }
    }
}
