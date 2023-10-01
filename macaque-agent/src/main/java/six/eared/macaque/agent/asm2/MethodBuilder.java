package six.eared.macaque.agent.asm2;

import six.eared.macaque.asm.MethodVisitor;

import java.util.function.Consumer;

public class MethodBuilder {

    private ClassBuilder classBuilder;

    private MethodVisitor methodVisitor;

    public MethodBuilder(ClassBuilder classBuilder, MethodVisitor methodVisitor) {
        this.classBuilder = classBuilder;
        this.methodVisitor = methodVisitor;
    }

    public ClassBuilder accept(Consumer<MethodVisitor> consumer) {
        consumer.accept(this.methodVisitor);
        return this.classBuilder;
    }
}
