package six.eared.macaque.agent.accessor;


import lombok.Data;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;

@Data
public class MethodDirectAccess implements MethodAccessRule {

    private MethodDirectAccess() {

    }

    public static final MethodAccessRule INSTANCE = new MethodDirectAccess();

    @Override
    public void access(InsnList insnList, int opcode, String owner, String name, String desc, boolean isInterface) {
        insnList.add(new MethodInsnNode(opcode, owner, name, desc, isInterface));
    }
}
