package six.eared.macaque.agent.asm2;

public class AsmField {

    private int modifier;

    private String fieldName;

    private String fieldSign;

    public boolean isNewField() {
        return false;
    }

    public boolean isDeleted() {
        return false;
    }

    public static final class AsmFieldBuilder {
        private int modifier;
        private String fieldName;
        private String fieldSign;

        private AsmFieldBuilder() {
        }

        public static AsmField.AsmFieldBuilder builder() {
            return new AsmField.AsmFieldBuilder();
        }

        public AsmField.AsmFieldBuilder modifier(int modifier) {
            this.modifier = modifier;
            return this;
        }

        public AsmField.AsmFieldBuilder fieldName(String FieldName) {
            this.fieldName = FieldName;
            return this;
        }

        public AsmField.AsmFieldBuilder fieldSign(String FieldSign) {
            this.fieldSign = FieldSign;
            return this;
        }

        public AsmField build() {
            AsmField asmField = new AsmField();
            asmField.modifier = this.modifier;
            asmField.fieldName = this.fieldName;
            asmField.fieldSign = this.fieldSign;
            return asmField;
        }
    }
}
