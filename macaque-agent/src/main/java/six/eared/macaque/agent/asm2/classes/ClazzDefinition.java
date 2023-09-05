package six.eared.macaque.agent.asm2.classes;

import six.eared.macaque.agent.asm2.AsmField;
import six.eared.macaque.agent.asm2.AsmMethod;

import java.util.ArrayList;
import java.util.List;

public class ClazzDefinition {

    private String className;

    private byte[] classData;

    private List<AsmMethod> asmMethods = new ArrayList<>();

    private List<AsmField> asmFields = new ArrayList<>();

    public ClazzDefinition() {

    }

    public ClazzDefinition(String className, byte[] classData) {
        this.className = className;
        this.classData = classData;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public byte[] getClassData() {
        return classData;
    }

    public void setClassData(byte[] classData) {
        this.classData = classData;
    }

    public void addAsmMethod(AsmMethod method) {
        asmMethods.add(method);
    }

    public List<AsmMethod> getAsmMethods() {
        return asmMethods;
    }

    public void addAsmField(AsmField asmField) {
        asmFields.add(asmField);
    }

    public List<AsmField> getAsmFields() {
        return asmFields;
    }
}
