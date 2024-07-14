package six.eared.macaque.agent.accessor;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class Accessor {
    public static final String FIELD_PREFIX = "macaque$field$";

    public static String accessField(String fieldName, String desc) {
        return FIELD_PREFIX + fieldName;
    }

    public static void accessField(MethodVisitor writer, int opcode, String owner, String name, String desc) {
        if (opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC) {

        } else {

        }
    }
}
