package six.eared.macaque.agent.asm2;

import io.github.hhy50.linker.asm.AsmClassBuilder;
import io.github.hhy50.linker.util.ClassUtil;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;
import java.util.Optional;

public class EnhancedAsmClassBuilder extends AsmClassBuilder {

    public EnhancedAsmClassBuilder() {
        super();
    }

    public void visit(int access, String className, String superName, String[] interfaces, String signature) {
        this.className = className;
        this.classOwner = ClassUtil.className2path(className);
        this.superOwner = Optional.ofNullable(superName).map(ClassUtil::className2path).orElse("java/lang/Object");
        this.members = new java.util.HashMap<>();
        this.getClassWriter()
                .visit(Opcodes.V1_8, access, this.classOwner, signature, this.superOwner,
                        Arrays.stream(interfaces == null ? new String[0] : interfaces).map(ClassUtil::className2path).toArray(String[]::new));
    }

    public ClassWriter getClassWriter() {
        if (this.classWriter == null) {
            this.classWriter = new ClassWriter(AUTO_COMPUTE);
        }
        return this.classWriter;
    }
}
