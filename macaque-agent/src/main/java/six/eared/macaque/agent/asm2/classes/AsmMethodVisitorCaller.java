package six.eared.macaque.agent.asm2.classes;

import six.eared.macaque.asm.MethodVisitor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class AsmMethodVisitorCaller implements InvocationHandler {

    private int index;

    private List<AsmMethodVisitCall> calls;

    public void accept(MethodVisitor mv) {
        for (AsmMethodVisitCall call : this.calls) {
            call.recall(mv);
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (calls == null) {
            this.calls = new ArrayList<>();
        }
        this.calls.add(new AsmMethodVisitCall()
                .index(++index)
                .callMethod(method)
                .args(args));
        return null;
    }
}
