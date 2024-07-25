package six.eared.macaque.agent.accessor;


import lombok.Data;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;


@Data
public class FieldForwardMethodAccess implements FieldAccessRule {
    private final boolean isStatic;
    private final String targetOwner;
    private final String getter;
    private final String setter;

    public FieldForwardMethodAccess(boolean isStatic, String targetOwner, String getter, String setter) {
        this.isStatic = isStatic;
        this.targetOwner = targetOwner;
        this.getter = getter;
        this.setter = setter;
    }


    @Override
    public void access(InsnList insnList, int opcode, String owner, String name, String type) {
        int no = isStatic?Opcodes.INVOKESTATIC:Opcodes.INVOKEVIRTUAL;
        if (opcode == Opcodes.PUTFIELD || opcode == Opcodes.PUTSTATIC) {
            insnList.add(new MethodInsnNode(no, targetOwner, setter, "("+Type.getType(type).getDescriptor()+")V", false));
        } else {
            insnList.add(new MethodInsnNode(no, targetOwner, getter, "()"+Type.getType(type).getDescriptor(), false));
        }
    }
}
