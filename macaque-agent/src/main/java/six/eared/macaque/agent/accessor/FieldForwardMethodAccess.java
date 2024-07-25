package six.eared.macaque.agent.accessor;


import javassist.bytecode.Opcode;
import lombok.Data;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;

@Data
public class FieldForwardMethodAccess implements FieldAccessRule {
    private final boolean isStatic;
    private final String targetOwner;
    private final String targetMethodName;
    private final String targetDesc;

    public FieldForwardMethodAccess(boolean isStatic, String targetOwner, String targetMethodName, String targetDesc) {
        this.isStatic = isStatic;
        this.targetOwner = targetOwner;
        this.targetMethodName = targetMethodName;
        this.targetDesc = targetDesc;
    }


    @Override
    public void access(InsnList insnList, int opcode, String owner, String name, String type) {
        opcode = isStatic?Opcode.INVOKESTATIC:Opcode.INVOKEVIRTUAL;
        insnList.add(new MethodInsnNode(opcode, targetOwner, targetMethodName, targetDesc, false));
    }
}
