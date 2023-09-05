package six.eared.macaque.agent.asm2.classes;

import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.asm.MethodVisitor;
import six.eared.macaque.asm.Opcodes;

import static six.eared.macaque.asm.Opcodes.ASM4;

public class AsmMethodReader extends MethodVisitor {

    private final AsmMethod asmMethod;

    public AsmMethodReader(AsmMethod asmMethod) {
        super(ASM4);
        this.asmMethod = asmMethod;
    }
    
    public void visitParameter(String name, int access) {
        this.asmMethod.addParameter(name, access);
    }
    
    public void visitCode() {
    }
    
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
    	// TODO Auto-generated method stub
    	super.visitMethodInsn(opcode, owner, name, desc, itf);
    }
    
    public void visitFrame(int type, int nLocal, Object[] local, int nStack,
            Object[] stack) {
    	System.out.println("type=" + type);
    }
}
