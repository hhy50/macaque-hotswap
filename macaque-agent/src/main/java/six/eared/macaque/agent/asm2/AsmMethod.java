package six.eared.macaque.agent.asm2;

import java.util.List;

public class AsmMethod {

    private int modifier;

    private String methodName;

    private String methodSign;

    private List<String> byteCode;
    
    private List<String> parameters;

    private boolean newMethod;

    private boolean deleted;

    public boolean isNewMethod() {
        return false;
    }

    public boolean isDeleted() {
        return false;
    }
    
    public void addParameter(String name, int access) {
    	this.parameters.add(name);
    }


    public static final class AsmMethodBuilder {
        private int modifier;
        private String methodName;
        private String methodSign;

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

        public AsmMethod build() {
            AsmMethod asmMethod = new AsmMethod();
            asmMethod.modifier = this.modifier;
            asmMethod.methodName = this.methodName;
            asmMethod.methodSign = this.methodSign;
            return asmMethod;
        }
    }
}
