package six.eared.macaque.agent.asm2.classes;

import six.eared.macaque.asm.IMethodVisitor;
import six.eared.macaque.asm.MethodVisitor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

public class AsmMethodVisitorCaller implements InvocationHandler {

    private int index;

    protected List<AsmMethodVisitCall> calls;

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
        this.calls.add(new AsmMethodVisitCall(++index, method, args));
        if (method.getName().equals("visitStart")) {
            visitStart();
        }
        if (method.getName().equals("visitEnd")) {
            visitEnd();
        }
        return null;
    }

    public MethodVisitor createProxyObj() {
        if (this.calls != null) this.calls = null;

        IMethodVisitor visitor = (IMethodVisitor) Proxy.newProxyInstance(AsmMethodVisitorCaller.class.getClassLoader(),
                new Class[]{IMethodVisitor.class}, this);
         return new MethodVisitorDelegation(visitor);
    }

    protected void visitStart() {

    }

    protected void visitEnd() {

    }

    public boolean isEmpty() {
        return this.calls == null || this.calls.isEmpty();
    }
}
