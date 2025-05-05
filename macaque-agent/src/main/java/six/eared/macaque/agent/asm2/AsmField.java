package six.eared.macaque.agent.asm2;


import lombok.Data;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

@Data
public class AsmField {

    private int modifier;

    private String fieldName;

    private String desc;

    private String fieldSign;

    private Object value;

    public Type getType() {
        return Type.getType(this.desc);
    }

    public boolean isPrivate() {
        return (modifier & Opcodes.ACC_PRIVATE) > 0;
    }

    public boolean isStatic() {
        return (modifier & Opcodes.ACC_STATIC) > 0;
    }

    public boolean isFinal() {
        return (modifier & Opcodes.ACC_FINAL) > 0;
    }

    public boolean isPublicForSub() {
        if ((this.modifier & Opcodes.ACC_PUBLIC) > 0) {
            return true;
        }
        if ((this.modifier & Opcodes.ACC_PROTECTED) > 0) {
            return true;
        }
        return false;
    }

    public static final class AsmFieldBuilder {
        private int modifier;
        private String fieldName;
        private String fieldDesc;
        private String fieldSign;
        private Object value;

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

        public AsmField.AsmFieldBuilder fieldDesc(String fieldDesc) {
            this.fieldDesc = fieldDesc;
            return this;
        }

        public AsmField.AsmFieldBuilder fieldSign(String fieldSign) {
            this.fieldSign = fieldSign;
            return this;
        }

        public AsmField.AsmFieldBuilder value(Object value) {
            this.value = value;
            return this;
        }

        public AsmField build() {
            AsmField asmField = new AsmField();
            asmField.modifier = this.modifier;
            asmField.desc = this.fieldDesc;
            asmField.fieldName = this.fieldName;
            asmField.fieldSign = this.fieldSign;
            asmField.value = this.value;
            return asmField;
        }
    }
}
