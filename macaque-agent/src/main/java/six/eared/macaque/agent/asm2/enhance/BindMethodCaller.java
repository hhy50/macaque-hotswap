package six.eared.macaque.agent.asm2.enhance;

import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.classes.AsmMethodVisitorCaller;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.asm2.classes.ClazzDefinitionVisitor;
import six.eared.macaque.agent.asm2.classes.CorrelationClazzDefinition;
import six.eared.macaque.agent.enums.CorrelationEnum;
import six.eared.macaque.asm.MethodVisitor;


public class BindMethodCaller extends AsmMethodVisitorCaller {

    private ClazzDefinition clazzDefinition;

    private AsmMethod asmMethod;

    public BindMethodCaller(ClazzDefinition clazzDefinition, AsmMethod asmMethod) {
        this.clazzDefinition = clazzDefinition;
        this.asmMethod = asmMethod;
    }

    @Override
    protected void visitEnd() {
        ClazzDefinitionVisitor visitor = new ClazzDefinitionVisitor() {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);

                if (name.equals(asmMethod.getMethodName()) && desc.equals(asmMethod.getDesc())) {
                    visitor = super.visitMethod(access, name, desc, signature, exceptions);




                    return null;
                }
                return visitor;
            }
        };
        this.clazzDefinition.putCorrelationClass(CorrelationClazzDefinition.bind(CorrelationEnum.METHOD_BIND,
                visitor.getDefinition()));
    }
}
