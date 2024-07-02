package six.eared.macaque.agent.asm2.classes;

import lombok.Data;
import six.eared.macaque.agent.asm2.AsmField;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.definition.Definition;
import six.eared.macaque.agent.enums.CorrelationEnum;
import six.eared.macaque.asm.ClassVisitor;

import java.util.ArrayList;
import java.util.List;

@Data
public class ClazzDefinition implements Cloneable, Definition {

    private String className;

    private String superClassName;

    private String[] interfaces;

    private byte[] originData;

    private byte[] byteCode;

    private List<CorrelationClazzDefinition> correlationClasses;

    private final List<AsmMethod> asmMethods = new ArrayList<>();

    private final List<AsmField> asmFields = new ArrayList<>();

    public void addAsmMethod(AsmMethod method) {
        this.asmMethods.add(method);
    }

    public void addAsmField(AsmField asmField) {
        this.asmFields.add(asmField);
    }

    public void addCorrelationClasses(CorrelationEnum correlation, ClazzDefinition definition) {
        if (this.correlationClasses == null) {
            this.correlationClasses = new ArrayList<>();
        }
        this.correlationClasses.add(CorrelationClazzDefinition.of(correlation, definition));
    }

    @Override
    public ClazzDefinition clone() {
        try {
            return (ClazzDefinition) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasMethod(String name, String desc) {
        return asmMethods.stream()
                .anyMatch(item -> item.getMethodName().equals(name) && item.getDesc().equals(desc));
    }

    public AsmMethod getMethod(String name, String desc) {
        return getMethod(name+"#"+desc);
    }

    public AsmMethod getMethod(String uniqueDesc) {
        return asmMethods.stream()
                .filter(item -> item.getUniqueDesc().equals(uniqueDesc))
                .findAny().get();
    }

    @Override
    public String getName() {
        return className;
    }

    @Override
    public String getFileType() {
        return "class";
    }

    @Override
    public byte[] getByteArray() {
        return byteCode;
    }

    public void revisit(ClassVisitor classVisitor) {
        AsmUtil.visitClass(this.byteCode, classVisitor);
    }

    public List<CorrelationClazzDefinition> getCorrelationClasses() {
        return correlationClasses;
    }

    public void putCorrelationClass(CorrelationClazzDefinition correlationClass) {
        if (this.correlationClasses == null) {
            this.correlationClasses = new ArrayList<>();
        }
        this.correlationClasses.add(correlationClass);
    }
}
