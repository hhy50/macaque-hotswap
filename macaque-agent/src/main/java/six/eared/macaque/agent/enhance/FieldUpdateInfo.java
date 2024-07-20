package six.eared.macaque.agent.enhance;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import six.eared.macaque.agent.asm2.AsmField;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FieldUpdateInfo {

    private AsmField asmField;

    public String getFieldName() {
        return asmField.getFieldName();
    }

    public String getDesc() {
        return asmField.getDesc();
    }
}
