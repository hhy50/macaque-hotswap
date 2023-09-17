package six.eared.macaque.agent.asm2;

import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.asm2.classes.ClazzDefinitionVisitor;
import six.eared.macaque.asm.ClassReader;

public class AsmUtil {

    /**
     * Thread Unsafe
     */
    public static final ClazzDefinitionVisitor REUSE_CLASS_VISITOR = new ClazzDefinitionVisitor();

    public synchronized static ClazzDefinition readClass(byte[] byteCode) {
        ClassReader classReader = new ClassReader(byteCode);
        classReader.accept(REUSE_CLASS_VISITOR, 0);
        return REUSE_CLASS_VISITOR.getDefinition();
    }
}
