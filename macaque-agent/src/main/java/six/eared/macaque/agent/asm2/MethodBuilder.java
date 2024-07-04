package six.eared.macaque.agent.asm2;

import six.eared.macaque.asm.MethodVisitor;

import java.util.function.Consumer;

public class MethodBuilder {

    private AsmClassBuilder classBuilder;

    private MethodVisitor methodVisitor;

    public MethodBuilder(AsmClassBuilder classBuilder, MethodVisitor methodVisitor) {
        this.classBuilder = classBuilder;
        this.methodVisitor = methodVisitor;
    }

    public AsmClassBuilder accept(Consumer<MethodVisitor> consumer) {
        consumer.accept(this.methodVisitor);
        return this.classBuilder;
    }

    public MethodVisitor getMethodVisitor() {
        return methodVisitor;
    }
}
