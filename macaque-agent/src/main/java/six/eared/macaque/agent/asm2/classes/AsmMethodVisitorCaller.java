package six.eared.macaque.agent.asm2.classes;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.MethodNode;

public class AsmMethodVisitorCaller {

    private MethodNode methodNode;

    public void accept(MethodVisitor mv) {
        if (this.methodNode != null)
            this.methodNode.accept(mv);
    }

    public MethodVisitor createProxyObj() {
        this.methodNode = new MethodNode();
        return this.methodNode;
    }

    public boolean isEmpty() {
        return this.methodNode == null
                || this.methodNode.instructions == null
                || this.methodNode.instructions.size() == 0;
    }
}
