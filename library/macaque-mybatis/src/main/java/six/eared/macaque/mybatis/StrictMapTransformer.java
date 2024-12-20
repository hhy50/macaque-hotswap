package six.eared.macaque.mybatis;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import six.eared.macaque.agent.asm2.AsmUtil;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import static org.objectweb.asm.Opcodes.ASM9;

public class StrictMapTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        String classPath = "org/apache/ibatis/session/Configuration$StrictMap";
        if (!className.equals(classPath)) {
            return new byte[0];
        }

        ClassNode classNode = new ClassNode();
        AsmUtil.visitClass(classfileBuffer, classNode);

        MethodNode putMethod = classNode.methods.stream().filter(item -> item.name.equals("put")).findAny().get();
        InsnList inst = new InsnList();
        inst.add(new VarInsnNode(Opcodes.ALOAD, 0));
        inst.add(new VarInsnNode(Opcodes.ALOAD, 1));
        inst.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, classPath, "remove", "(Ljava/lang/Object;)Ljava/lang/Object;"));
        inst.add(new InsnNode(Opcodes.POP));
        putMethod.instructions.insert(inst);

        ClassWriter classWriter = new ClassWriter(ASM9);
        classNode.accept(classWriter);

        return classWriter.toByteArray();
    }
}
