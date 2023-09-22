package six.eared.macaque.agent.asm2.enhance;

import java.util.Objects;

public class MethodBindInfo {

    private String bindClass;

    private String bindMethod;

    public String getBindClass() {
        return bindClass;
    }

    public String getBindMethod() {
        return bindMethod;
    }

    public void setBindClass(String bindClass) {
        this.bindClass = bindClass;
    }

    public void setBindMethod(String bindMethod) {
        this.bindMethod = bindMethod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodBindInfo that = (MethodBindInfo) o;
        return Objects.equals(bindClass, that.bindClass)
                && Objects.equals(bindMethod, that.bindMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bindClass, bindMethod);
    }
}
