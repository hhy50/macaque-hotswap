package six.eared.macaque.agent.asm2;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.asm2.classes.ClazzDefinitionVisitor;
import six.eared.macaque.agent.asm2.classes.ClazzDefinitionVisitorFactory;
import six.eared.macaque.agent.asm2.classes.MultiClassReader;
import six.eared.macaque.agent.enhance.ClazzDataDefinition;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.common.util.ClassUtil;
import six.eared.macaque.common.util.CollectionUtil;
import six.eared.macaque.common.util.FileUtil;
import six.eared.macaque.common.util.InstrumentationUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AsmUtil {

    /**
     * Thread Unsafe
     */
    public static final ClazzDefinitionVisitor REUSE_CLASS_VISITOR = new ClazzDefinitionVisitor();


    /**
     * 对于没有class文件的class会抛出ClassNotFoundException
     *
     * @param className
     * @return
     * @throws ClassNotFoundException
     */
    public static ClazzDefinition readOriginClass(String className) throws ClassNotFoundException, IOException {
        try (InputStream is = ClassLoader.getSystemResourceAsStream(ClassUtil.className2path(className));) {
            if (is != null) {
                return AsmUtil.readClass(FileUtil.is2bytes(is));
            }
        } catch (IOException e) {
            if (Environment.isDebug()) {
                System.out.println("findLastView error");
                e.printStackTrace();
            }
            throw e;
        }
        Set<Class<?>> loadedClass = InstrumentationUtil.findLoadedClass(Environment.getInst(), className);
        if (CollectionUtil.isNotEmpty(loadedClass)) {
            Class<?> clazz = loadedClass.iterator().next();
            return new ClazzDefinition.InMemory(clazz);
        }
        throw new ClassNotFoundException();
    }

    public synchronized static ClazzDataDefinition readClass(byte[] byteCode) {
        ClassReader classReader = new ClassReader(byteCode);
        classReader.accept(REUSE_CLASS_VISITOR, 0);
        ClazzDataDefinition definition = REUSE_CLASS_VISITOR.getDefinition();
        definition.setBytecode(byteCode);
        return definition;
    }

    public static void visitClass(byte[] byteCode, ClassVisitor classVisitor) {
        ClassReader classReader = new ClassReader(byteCode);
        classReader.accept(classVisitor, 0);
    }

    public static List<ClazzDataDefinition> readMultiClass(byte[] byteCode) {
        MultiClassReader multiClassReader = new MultiClassReader(byteCode, ClazzDefinitionVisitorFactory.DEFAULT);
        List<ClazzDataDefinition> definitions = new ArrayList<>();
        for (ClazzDataDefinition definition : multiClassReader) {
            definitions.add(definition);
        }
        return definitions;
    }

    public static AsmClassBuilder defineClass(int access, String className, String superName, String[] interfaces, String sign) {
        return new AsmClassBuilder()
                .defineClass(access, className, superName, interfaces, sign);
    }

    public static void areturn(MethodVisitor writer, Type rType) {
        if (rType.getSort() == Type.VOID) {
            writer.visitInsn(Opcodes.RETURN);
        } else {
            writer.visitInsn(rType.getOpcode(Opcodes.IRETURN));
        }
    }

    public static String toTypeDesc(String className) {
        return "L" + ClassUtil.simpleClassName2path(className) + ";";
    }

    public static String addArgsDesc(String methodDesc, String newArg, boolean header) {
        String delimiter = header ? "\\(" : "\\)";
        String[] split = methodDesc.split(delimiter);
        split[0] += AsmUtil.toTypeDesc(newArg);
        return header ? ("(" + split[0] + split[1]) : (split[0] + ")" + split[1]);
    }

    public static void throwNoSuchMethod(MethodVisitor write, String methodName) {
        write.visitTypeInsn(Opcodes.NEW, "java/lang/NoSuchMethodError");
        write.visitInsn(Opcodes.DUP);
        write.visitLdcInsn(methodName);
        write.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/NoSuchMethodError", "<init>", "(Ljava/lang/String;)V", false);
        write.visitInsn(Opcodes.ATHROW);
    }

    /**
     * 计算本地变量表长度
     *
     * @param argumentTypes
     * @return
     */
    public static int calculateLvbOffset(boolean isStatic, Type[] argumentTypes) {
        int lvbLen = isStatic ? 0 : 1; // this
        for (Type argumentType : argumentTypes) {
            int setup = argumentType.getSort() == Type.DOUBLE || argumentType.getSort() == Type.LONG ? 2 : 1;
            lvbLen += setup;
        }
        return lvbLen;
    }

    /**
     * 访问器入栈
     */
    public static void accessorStore(MethodVisitor writer, String accessorDesc) {
        writer.visitTypeInsn(Opcodes.NEW, accessorDesc);
        writer.visitInsn(Opcodes.DUP);
        writer.visitVarInsn(Opcodes.ALOAD, 0);
        writer.visitMethodInsn(Opcodes.INVOKESPECIAL, accessorDesc, "<init>", "(Ljava/lang/Object;)V", false);
    }

    public static void accessorStore(InsnList instList, String accessorDesc) {
        instList.add(new TypeInsnNode(Opcodes.NEW, accessorDesc));
        instList.add(new InsnNode(Opcodes.DUP));
        instList.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instList.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, accessorDesc, "<init>", "(Ljava/lang/Object;)V", false));
    }

    /**
     * 获取上n个参数入栈的指令
     * @param
     * @param current
     */
    public static AbstractInsnNode getPrevStackInsn(int n, AbstractInsnNode current) {
        AbstractInsnNode prev = current;
        while (n > 0) {
            prev = getPrev(prev);
            if (prev instanceof MethodInsnNode) {
                int invoke = prev.getOpcode();
                String invokeName = ((MethodInsnNode) prev).name;
                n += Type.getArgumentTypes(((MethodInsnNode) prev).desc).length;
                if (invokeName.equals("<init>")) {
                    // new
                    // dup
                    n += 2;
                } else if (invoke != Opcodes.INVOKESTATIC) {
                    n += 1;
                }
            }
            prev = prev.getPrevious();
            n--;
        }
        return prev;
    }

    /**
     * 获取上一条指令
     * @param current
     * @return
     */
    public static AbstractInsnNode getPrev(AbstractInsnNode current) {
        AbstractInsnNode prev = current;
        while (prev instanceof LineNumberNode || prev instanceof LabelNode) {
            prev = prev.getPrevious();
        }
        return prev;
    }
}