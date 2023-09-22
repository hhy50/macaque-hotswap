package six.eared.macaque.agent.asm2.classes;

import six.eared.macaque.agent.asm2.enhance.CompatibilityModeMethodVisitor;
import six.eared.macaque.asm.IMethodVisitor;
import six.eared.macaque.asm.MethodVisitor;
import six.eared.macaque.asm.Opcodes;

import java.lang.reflect.Proxy;

public class MethodVisitorProxy extends MethodVisitor {

    private AsmMethodVisitorCaller caller = new AsmMethodVisitorCaller();

    public MethodVisitorProxy() {
        super(Opcodes.ASM5);
        this.mv = createProxyObj();
    }

    public MethodVisitorProxy(MethodVisitor mv) {
        super(Opcodes.ASM5, mv);
    }

    public void revisit(MethodVisitor mv) {
        caller.accept(mv);
    }

    protected IMethodVisitor createProxyObj() {
        return (IMethodVisitor) Proxy.newProxyInstance(CompatibilityModeMethodVisitor.class.getClassLoader(),
                new Class[]{IMethodVisitor.class}, caller);
    }
}
