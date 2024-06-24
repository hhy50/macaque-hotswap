package six.eared.macaque.agent.asm2;

import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.asm2.classes.ClazzDefinitionVisitor;
import six.eared.macaque.agent.asm2.classes.ClazzDefinitionVisitorFactory;
import six.eared.macaque.agent.asm2.classes.MultiClassReader;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.asm.*;
import six.eared.macaque.common.util.ClassUtil;
import six.eared.macaque.common.util.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
        throw new ClassNotFoundException();
    }

    public synchronized static ClazzDefinition readClass(byte[] byteCode) {
        ClassReader classReader = new ClassReader(byteCode);
        classReader.accept(REUSE_CLASS_VISITOR, 0);
        return REUSE_CLASS_VISITOR.getDefinition();
    }

    public static ClazzDefinition readClass(byte[] byteCode, ClazzDefinitionVisitor clazzVisitor) {
        visitClass(byteCode, clazzVisitor);
        return clazzVisitor.getDefinition();
    }

    public static void visitClass(byte[] byteCode, ClassVisitor classVisitor) {
        ClassReader classReader = new ClassReader(byteCode);
        classReader.accept(classVisitor, 0);
    }

    public static List<ClazzDefinition> readMultiClass(byte[] byteCode, ClazzDefinitionVisitorFactory factory) {
        MultiClassReader multiClassReader = new MultiClassReader(byteCode, factory);
        List<ClazzDefinition> definitions = new ArrayList<>();
        for (ClazzDefinition definition : multiClassReader) {
            definitions.add(definition);
        }
        return definitions;
    }

    public static String accessToDescriptor(int access) {
        StringBuilder sb = new StringBuilder();

        if ((access & Opcodes.ACC_PUBLIC) != 0) {
            sb.append("public ");
        } else if ((access & Opcodes.ACC_PRIVATE) != 0) {
            sb.append("private ");
        } else if ((access & Opcodes.ACC_PROTECTED) != 0) {
            sb.append("protected ");
        }

        if ((access & Opcodes.ACC_STATIC) != 0) {
            sb.append("static ");
        }

        if ((access & Opcodes.ACC_INTERFACE) != 0) {
            sb.append("interface ");
        } else if ((access & Opcodes.ACC_ENUM) != 0) {
            sb.append("enum ");
        } else if ((access & Opcodes.ACC_ANNOTATION) != 0) {
            sb.append("@interface ");
        }
        return sb.toString();
    }

    public static ClassBuilder defineClass(int access, String className, String superName, String[] interfaces, String sign) {
        return new ClassBuilder()
                .defineClass(access, className, superName, interfaces, sign);
    }

    public static void areturn(MethodVisitor writer, Type rType) {
        if (rType.getReturnType().getSort() == Type.VOID) {
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

    public static String methodDesc(String rDesc, String... pDesc) {
        String desc = "";
        for (String s : pDesc) {
            desc += s;
        }
        return "(" + desc + ")" + rDesc;
    }
}
