package six.eared.macaque.agent.accessor;


import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;

public class FieldDirectAccess implements FieldAccessRule {
    public static final FieldDirectAccess INSTANCE = new FieldDirectAccess();

    private FieldDirectAccess() {

    }

    @Override
    public void access(InsnList insnList, int opcode, String owner, String name, String type) {
        insnList.add(new FieldInsnNode(opcode, owner, name, type));
    }
}
