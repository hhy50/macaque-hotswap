package six.eared.macaque.agent.asm2;

import java.util.List;

public class AsmMethod {

    private int modifier;

    private String methodName;

    private String methodSign;

    private List<String> byteCode;

    private boolean newMethod;

    private boolean deleted;


    public static final class AsmMethodBuilder {
        private int modifier;
        private String methodName;
        private String methodSign;
        private List<String> byteCode;
        private boolean newMethod;
        private boolean deleted;

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

        public AsmMethodBuilder byteCode(List<String> byteCode) {
            this.byteCode = byteCode;
            return this;
        }

        public AsmMethodBuilder newMethod(boolean newMethod) {
            this.newMethod = newMethod;
            return this;
        }

        public AsmMethodBuilder deleted(boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        public AsmMethod build() {
            AsmMethod asmMethod = new AsmMethod();
            asmMethod.byteCode = this.byteCode;
            asmMethod.methodName = this.methodName;
            asmMethod.deleted = this.deleted;
            asmMethod.newMethod = this.newMethod;
            asmMethod.modifier = this.modifier;
            asmMethod.methodSign = this.methodSign;
            return asmMethod;
        }
    }
}
