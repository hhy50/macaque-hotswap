package six.eared.macaque.agent.enhance;

import lombok.Data;
import six.eared.macaque.agent.accessor.MethodAccessRule;
import six.eared.macaque.agent.accessor.NewMethodAccessRule;

import java.util.Objects;


@Data
public class MethodBindInfo implements Cloneable {

    /**
     * 目标类
     */
    private String bindClass;

    /**
     * 目标方法
     */
    private String bindMethod;

    /**
     * 方法描述
     */
    private String bindMethodDesc;

    /**
     * 访问器名称
     */
    private String accessorClass;

    /**
     * 是否是静态方法
     */
    private boolean isStatic;

    /**
     * 是否加载
     */
    private boolean loaded;

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

    public MethodAccessRule getAccessRule(boolean bindAccess) {
        if (isStatic) {
            return MethodAccessRule.forward(true, bindClass, bindMethod, bindMethodDesc);
        }
        return new NewMethodAccessRule(this, bindAccess);
    }
}
