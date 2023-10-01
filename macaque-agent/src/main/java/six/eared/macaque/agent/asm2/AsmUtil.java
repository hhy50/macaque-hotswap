package six.eared.macaque.agent.asm2;

import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.asm2.classes.ClazzDefinitionVisitor;
import six.eared.macaque.agent.asm2.classes.ClazzDefinitionVisitorFactory;
import six.eared.macaque.agent.asm2.classes.MultiClassReader;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.asm.ClassReader;
import six.eared.macaque.asm.Opcodes;
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

    public static ClazzDefinition readOriginClass(String className) throws ClassNotFoundException {
        try (InputStream is = ClassLoader.getSystemResourceAsStream(ClassUtil.className2path(className));) {
            if (is != null) {
                return AsmUtil.readClass(FileUtil.is2bytes(is));
            }
        } catch (IOException e) {
            if (Environment.isDebug()) {
                System.out.println("findLastView error");
                e.printStackTrace();
            }
        }
        throw new ClassNotFoundException();
    }

    public synchronized static ClazzDefinition readClass(byte[] byteCode) {
        ClassReader classReader = new ClassReader(byteCode);
        classReader.accept(REUSE_CLASS_VISITOR, 0);
        return REUSE_CLASS_VISITOR.getDefinition();
    }

    public static ClazzDefinition readClass(byte[] byteCode, ClazzDefinitionVisitor clazzVisitor) {
        ClassReader classReader = new ClassReader(byteCode);
        classReader.accept(clazzVisitor, 0);
        return clazzVisitor.getDefinition();
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
        return sb.toString().trim();
    }
}
