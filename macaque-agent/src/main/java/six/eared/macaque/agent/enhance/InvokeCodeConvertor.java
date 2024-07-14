package six.eared.macaque.agent.enhance;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.classes.MethodDynamicStackVisitor;
import six.eared.macaque.common.util.ClassUtil;

/**
 * 改变调用指令的字节码转换器
 */

public class InvokeCodeConvertor extends MethodDynamicStackVisitor {

    private final AsmMethod method;
    private final MethodVisitor write;

    public InvokeCodeConvertor(AsmMethod method, MethodVisitor write) {
        this.method = method;
        this.write = write;
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        // 加1 是需要访问器入栈
        super.visitMaxs(maxStack + 1, maxLocals);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        MethodBindInfo bindInfo = MethodBindManager.getBindInfo(ClassUtil.classpath2name(owner), name,
                desc, opcode == Opcodes.INVOKESTATIC);
        if (bindInfo != null) {
            String accessorClassPath = ClassUtil.simpleClassName2path(bindInfo.getAccessorClass());
            if (opcode == Opcodes.INVOKESTATIC) {
                // 只需要访问器入栈
                AsmUtil.accessorStore(this, accessorClassPath);
            } else {
                /**
                 * 需要访问器提前入栈
                 * 先找到压入参数之前的第一条指令
                 */
                Type[] argsType = Type.getArgumentTypes(desc);
                AbstractInsnNode prev = this.instructions.getLast();
                int n = argsType.length;
                while (n > 0) {
                    if (prev instanceof MethodInsnNode) {
                        int invoke = ((MethodInsnNode) prev).getOpcode();
                        String invokeName = ((MethodInsnNode) prev).name;
                        n += Type.getArgumentTypes(((MethodInsnNode) prev).desc).length;
                        if (invokeName.equals("<init>")) {
                            // new
                            // dup
                            n += 2;
                        } else if (invoke != Opcodes.INVOKESTATIC) {
                            n += 1;
                        }
                    } else if (prev instanceof LineNumberNode || prev instanceof LabelNode) {
                        prev = prev.getPrevious();
                        continue;
                    }
                    prev = prev.getPrevious();
                    n--;
                }

                InsnList inst = new InsnList();
                // 需要先将obj弹出
                inst.add(new InsnNode(Opcodes.POP));
                // 访问器入栈
                AsmUtil.accessorStore(inst, accessorClassPath);
                this.instructions.insert(prev, inst);
            }
            super.visitMethodInsn(Opcodes.INVOKESTATIC, ClassUtil.simpleClassName2path(bindInfo.getBindClass()), bindInfo.getBindMethod(),
                    bindInfo.getBindMethodDesc(), itf);
            return;
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        this.accept(write);
    }
}
