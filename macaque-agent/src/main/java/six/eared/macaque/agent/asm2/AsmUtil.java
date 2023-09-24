package six.eared.macaque.agent.asm2;

import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.asm2.classes.ClazzDefinitionVisitor;
import six.eared.macaque.agent.asm2.classes.ClazzDefinitionVisitorFactory;
import six.eared.macaque.agent.asm2.classes.MultiClassReader;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.asm.ClassReader;
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

    public synchronized static ClazzDefinition readOriginClass(String className) throws ClassNotFoundException {
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

    public synchronized static ClazzDefinition readClass(byte[] byteCode, ClazzDefinitionVisitorFactory factory) {
        ClazzDefinitionVisitor clazzDefinitionVisitor = factory.creatClazzVisitor();
        ClassReader classReader = new ClassReader(byteCode);
        classReader.accept(clazzDefinitionVisitor, 0);
        return clazzDefinitionVisitor.getDefinition();
    }

    public synchronized static List<ClazzDefinition> readMultiClass(byte[] byteCode, ClazzDefinitionVisitorFactory factory) {
        MultiClassReader multiClassReader = new MultiClassReader(byteCode, factory);
        List<ClazzDefinition> definitions = new ArrayList<>();
        for (ClazzDefinition definition : multiClassReader) {
            definitions.add(definition);
        }
        return definitions;
    }
}
