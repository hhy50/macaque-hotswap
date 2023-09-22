package six.eared.macaque.agent.asm2.classes;

import six.eared.macaque.asm.MethodVisitor;

import java.lang.reflect.Method;

public class AsmMethodVisitCall {

    private int index;

    private Method callMethod;

    private Object[] args;

    public AsmMethodVisitCall(int index, Method callMethod, Object[] args) {
        this.index = index;
        this.callMethod = callMethod;
        this.args = args;
    }


    public Object recall(MethodVisitor mv) {
        try {
            return this.callMethod.invoke(mv, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
