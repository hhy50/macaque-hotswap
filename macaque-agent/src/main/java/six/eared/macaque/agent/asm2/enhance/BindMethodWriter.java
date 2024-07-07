package six.eared.macaque.agent.asm2.enhance;


import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.asm.MethodVisitor;
import six.eared.macaque.asm.Opcodes;

public class BindMethodWriter extends MethodVisitor {

    private AsmMethod method;

    private MethodBindInfo bindInfo;

    public BindMethodWriter(MethodVisitor writer, AsmMethod method, MethodBindInfo bindInfo) {
        super(Opcodes.ASM5, writer);
        this.method = method;
        this.bindInfo = bindInfo;
    }

    /**
     *       stack=3, locals=3, args_size=3
     *          0: new           #8                  // class java/lang/StringBuilder
     *          3: dup
     *          4: invokespecial #12                 // Method java/lang/StringBuilder."<init>":()V
     *          7: ldc           #14                 // String arg1=
     *          9: invokevirtual #18                 // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
     *         12: aload_0
     *         13: invokevirtual #18                 // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
     *         16: ldc           #20                 // String ,arg2=
     *         18: invokevirtual #18                 // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
     *         21: aload_1
     *         22: invokevirtual #18                 // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
     *         25: invokevirtual #24                 // Method java/lang/StringBuilder.toString:()Ljava/lang/String;
     *         28: areturn
     *
     *
     * @param maxStack
     *            maximum stack size of the method.
     * @param maxLocals
     *            maximum number of local variables for the method.
     */
    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        if (method.isStatic()) {
            maxStack += 1;
            maxLocals += 1;
        }
        super.visitMaxs(maxStack, maxLocals);
    }
}
