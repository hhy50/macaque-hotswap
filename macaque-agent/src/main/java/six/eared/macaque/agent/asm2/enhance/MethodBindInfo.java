package six.eared.macaque.agent.asm2.enhance;

import java.util.Objects;

public class MethodBindInfo implements Cloneable {

    private String bindClass;

    private String bindMethod;

    private boolean privateMethod;

    private String accessorClassName;

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

    public boolean isPrivateMethod() {
        return privateMethod;
    }

    public void setPrivateMethod(boolean privateMethod) {
        this.privateMethod = privateMethod;
    }

    public String getAccessorClassName() {
        return accessorClassName;
    }

    public void setAccessorClassName(String accessorClassName) {
        this.accessorClassName = accessorClassName;
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

    @Override
    public MethodBindInfo clone() {
        try {
            return (MethodBindInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
