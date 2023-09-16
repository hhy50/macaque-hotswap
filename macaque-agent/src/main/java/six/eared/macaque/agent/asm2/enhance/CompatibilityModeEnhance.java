package six.eared.macaque.agent.asm2.enhance;

import six.eared.macaque.agent.asm2.AsmField;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.Enhancer;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.asm2.classes.ClazzDefinitionVisitor;
import six.eared.macaque.agent.vcs.VersionChainAccessor;
import six.eared.macaque.agent.vcs.VersionDescriptor;
import six.eared.macaque.agent.vcs.VersionView;
import six.eared.macaque.asm.ClassReader;
import six.eared.macaque.common.util.ClassUtil;
import six.eared.macaque.common.util.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public class CompatibilityModeEnhance implements Enhancer {

    private ThreadLocal<ClazzDefinitionVisitor> VISITOR = new ThreadLocal<ClazzDefinitionVisitor>() {
        @Override
        protected ClazzDefinitionVisitor initialValue() {
            return new ClazzDefinitionVisitor(new CompatibilityModeMethodVisitor(), new CompatibilityModeFieldVisitor());
        }
    };

    @Override
    public ClazzDefinition enhance(ClazzDefinition newClassDefinition) {

        String className = newClassDefinition.getClassName();

        byte[] memoryClassData = null;
        VersionDescriptor lastVd = VersionChainAccessor.getLastVersion(className);
        if (lastVd != null) {
            VersionView versionView = VersionChainAccessor.getVersionView(lastVd);
            Optional<ClazzDefinition> any = versionView.getDefinitions().stream()
                    .filter(item -> item.getClassName().equals(className)).findAny();
            if (any.isPresent()) {
                memoryClassData = any.get().getByteCode();
            }
        } else {
            InputStream is = ClassLoader.getSystemResourceAsStream(ClassUtil.className2path(className));
            if (is != null) {
                try {
                    memoryClassData = FileUtil.is2bytes(is);
                } finally {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        if (memoryClassData != null) {
            ClazzDefinition oldClazzDefinition = readClass(memoryClassData);

            // TODO compare
            // 方法差集
            List<AsmMethod> methodDiffSet = compareMethod(newClassDefinition, oldClazzDefinition);
            if (methodDiffSet != null) {
                for (AsmMethod asmMethod : methodDiffSet) {

                }
            }

            List<AsmField> fieldDiffSet = compareField(newClassDefinition, oldClazzDefinition);
            if (fieldDiffSet != null) {
                for (AsmField asmField : fieldDiffSet) {
                    // 新增字段
                    if (asmField.isNewField()) {

                    }
                    // 删除字段
                    if (asmField.isDeleted()) {

                    }
                }
            }
        }
        return null;
    }

    private List<AsmField> compareField(ClazzDefinition newClassDefinition, ClazzDefinition oldClazzDefinition) {
        return null;
    }

    private List<AsmMethod> compareMethod(ClazzDefinition newClassDefinition, ClazzDefinition oldClazzDefinition) {
        return null;
    }

    private ClazzDefinition readClass(byte[] bytes) {
        try {
            ClazzDefinitionVisitor clazzDefinitionVisitor = new ClazzDefinitionVisitor();
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(clazzDefinitionVisitor, 0);
            return clazzDefinitionVisitor.getDefinition();
        } finally {
            VISITOR.remove();
        }
    }
}
