package six.eared.macaque.agent.accessor;

import io.github.hhy50.linker.asm.AsmClassBuilder;
import io.github.hhy50.linker.asm.MethodBuilder;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.objectweb.asm.Opcodes;
import six.eared.macaque.agent.asm2.AsmField;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.ClassFieldUniqueDesc;
import six.eared.macaque.agent.asm2.ClassMethodUniqueDesc;
import six.eared.macaque.common.util.FileUtil;
import six.eared.macaque.common.util.Maps;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


public class AccessorClassBuilder extends AsmClassBuilder {

    private static final String METHOD_NAME_ANNO = "Lio/github/hhy50/linker/annotations/Method$Name;";
    private static final String INVOKESUPER_ANNO = "Lio/github/hhy50/linker/annotations/Method$InvokeSuper;";
    private static final String TARGET_BIND_ANNO = "Lio/github/hhy50/linker/annotations/Target$Bind;";

    @Setter
    @Accessors(chain = true)
    private Accessor parent;
    private String this$0;

    private AsmClassBuilder linkerClassBuilder;
    private Map<ClassMethodUniqueDesc, MethodAccessRule> methodAccessRules = new HashMap<>();
    private Map<ClassFieldUniqueDesc, FieldAccessRule> fieldAccessRules = new HashMap<>();

    /**
     * Instantiates a new Asm class builder.
     *
     * @param className  the class name
     * @param superName  the super name
     * @param interfaces the interfaces
     */
    public AccessorClassBuilder(String className, String superName, String[] interfaces) {
        super(Opcodes.ACC_PUBLIC|Opcodes.ACC_INTERFACE, className, superName, interfaces, null);
    }

    public AccessorClassBuilder setThis$0(String this$0) {
        this.this$0 = this$0;
        this.linkerClassBuilder = new AsmClassBuilder(Opcodes.ACC_PUBLIC|Opcodes.ACC_INTERFACE, className+"$Linker", className, null, null);
        this.linkerClassBuilder.addAnnotation(TARGET_BIND_ANNO, Maps.of("value", this$0));
        return this;
    }

    public void addMethod(String owner, AsmMethod method) {
        String methodName = className.replace('.','_')+method.getMethodName();
        MethodBuilder methodBuilder = super.defineMethod(Opcodes.ACC_PUBLIC| Opcodes.ACC_ABSTRACT, methodName, method.getDesc(), method.getExceptions());
        methodBuilder.addAnnotation(METHOD_NAME_ANNO, Maps.of("value", method.getMethodName()));

        MethodAccessRule rule = null;
        if (method.isStatic()) {
            rule = invokerStatic(owner, method);
        } else {
            if (!className.equals(this$0) && parent == null) {
                methodBuilder.addAnnotation(INVOKESUPER_ANNO, Maps.of("value", className));
            }
            rule = invokeInstance(owner, method);
        }

        this.methodAccessRules.put(ClassMethodUniqueDesc.of(owner, method.getMethodName(), method.getDesc()), rule);
    }

    private MethodAccessRule invokerStatic(String owner, AsmMethod method) {

        return null;
    }

    private MethodAccessRule invokeInstance(String owner, AsmMethod method) {

        return null;
    }

    public void addField(String className, AsmField asmField) {
        //super.defineMethod(Opcodes.ACC_PUBLIC| Opcodes.ACC_ABSTRACT, methodName, method.getDesc(), method.getExceptions());
    }

    public Accessor toAccessor() {
        byte[] bytecode = this.toBytecode();
        FileUtil.writeBytes(new File(FileUtil.getProcessTmpPath()+"/compatibility/aaa.class"), bytecode);
        return null;
    }
}
