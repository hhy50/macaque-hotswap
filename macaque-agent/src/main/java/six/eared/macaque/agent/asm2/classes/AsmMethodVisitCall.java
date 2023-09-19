package six.eared.macaque.agent.asm2.classes;

import six.eared.macaque.asm.MethodVisitor;

import java.lang.reflect.Method;

public class AsmMethodVisitCall {

    private int index;

    private Method callMethod;

    private Object[] args;

    public AsmMethodVisitCall index(int index) {
        this.index = index;
        return this;
    }

    public AsmMethodVisitCall callMethod(Method callMethod) {
        this.callMethod = callMethod;
        return this;
    }

    public AsmMethodVisitCall args(Object... args) {
        this.args = args;
        return this;
    }

    public Object recall(MethodVisitor mv) {
        try {
            return this.callMethod.invoke(mv, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
