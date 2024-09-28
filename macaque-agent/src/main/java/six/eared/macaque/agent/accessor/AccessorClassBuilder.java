package six.eared.macaque.agent.accessor;

import io.github.hhy50.linker.asm.AsmClassBuilder;
import io.github.hhy50.linker.asm.AsmUtil;
import io.github.hhy50.linker.asm.MethodBuilder;
import io.github.hhy50.linker.define.MethodDescriptor;
import io.github.hhy50.linker.generate.MethodBody;
import io.github.hhy50.linker.generate.bytecode.Member;
import io.github.hhy50.linker.generate.bytecode.action.Actions;
import io.github.hhy50.linker.generate.bytecode.action.LdcLoadAction;
import io.github.hhy50.linker.generate.bytecode.action.MethodInvokeAction;
import lombok.Getter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import six.eared.macaque.agent.asm2.AsmField;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.ClassFieldUniqueDesc;
import six.eared.macaque.agent.asm2.ClassMethodUniqueDesc;
import six.eared.macaque.common.util.Maps;

import java.util.HashMap;
import java.util.Map;


public class AccessorClassBuilder extends AsmClassBuilder {

    private static final String METHOD_NAME_ANNO = "Lio/github/hhy50/linker/annotations/Method$Name;";
    private static final String INVOKESUPER_ANNO = "Lio/github/hhy50/linker/annotations/Method$InvokeSuper;";
    private static final String TARGET_BIND_ANNO = "Lio/github/hhy50/linker/annotations/Target$Bind;";
    private static final String LINKER_FIELD_NAME = "_linker";
    private static final String STATIC_LINKER_FIELD_NAME = "_static_linker";

    private Accessor parent;
    private String this$0;
    private MethodBody init;
    @Getter
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
        super(Opcodes.ACC_PUBLIC, className, superName, interfaces, null);
    }

    public AccessorClassBuilder setThis$0(String this$0) {
        this.this$0 = this$0;
        this.linkerClassBuilder = new AsmClassBuilder(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE, className+"$Linker", null, null, null);
        this.linkerClassBuilder.addAnnotation(TARGET_BIND_ANNO, Maps.of("value", this$0));
        return this;
    }

    public AccessorClassBuilder setParent(Accessor parent) {
        this.parent = parent;
        // 创建构造函数
//        this.defineConstruct(Opcodes.ACC_PUBLIC, new String[]{"java.lang.Object;"}, null, null)
//                .accept(body -> );
        return this;
    }

    public void addMethod(String owner, AsmMethod method) {
        String methodName = owner.replace('.', '_')+"_"+method.getMethodName();
        MethodBuilder methodBuilder = linkerClassBuilder.defineMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, methodName, method.getDesc(), method.getExceptions());
        methodBuilder.addAnnotation(METHOD_NAME_ANNO, Maps.of("value", method.getMethodName()));

        MethodAccessRule rule = null;
        if (method.isStatic()) {
            rule = invokeStatic(owner, methodName, method);
        } else {
            if (!owner.equals(this$0) && parent == null) {
                methodBuilder.addAnnotation(INVOKESUPER_ANNO, Maps.of("value", owner));
            }
            rule = invokeInstance(owner, method);
        }
        this.methodAccessRules.put(ClassMethodUniqueDesc.of(owner, method.getMethodName(), method.getDesc()), rule);
    }

    private MethodAccessRule invokeStatic(String owner, String methodName, AsmMethod method) {
        if (method.isPrivate()) {
            super.defineMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, methodName, method.getDesc(), method.getExceptions()).accept(body ->
                    body.append(new MethodInvokeAction(MethodDescriptor.of(methodName, method.getDesc()))
                            .setInstance(Member.ofStatic(STATIC_LINKER_FIELD_NAME, AsmUtil.getType(linkerClassBuilder.getClassName())))
                            .setArgs(Actions.loadArgs())
                            .thenReturn()
                    ));
            return MethodAccessRule.forward(true, this.getClassName(), methodName, method.getDesc());
        }
        return MethodAccessRule.direct();
    }

    private MethodAccessRule invokeInstance(String owner, AsmMethod method) {

        return null;
    }

    public void addField(String className, AsmField asmField) {
        //super.defineMethod(Opcodes.ACC_PUBLIC| Opcodes.ACC_ABSTRACT, methodName, method.getDesc(), method.getExceptions());
    }

    public AccessorClassBuilder end() {
        MethodBody clinit = getClinit();
        Type linkerType = AsmUtil.getType(linkerClassBuilder.getClassName());
        Type outType = AsmUtil.getType(className);

        this.defineField(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, STATIC_LINKER_FIELD_NAME, linkerType, null, null)
                .store(clinit, new MethodInvokeAction(MethodDescriptor.LINKER_FACTORY_CREATE_LINKER).setArgs(
                        LdcLoadAction.of(linkerType),
                        new MethodInvokeAction(MethodDescriptor.GET_CLASS_LOADER).setInstance(LdcLoadAction.of(outType))
                ));
        this.defineField(Opcodes.ACC_PRIVATE, LINKER_FIELD_NAME, linkerType, null, null);

        return (AccessorClassBuilder) super.end();
    }

    public Accessor toAccessor() {
        byte[] bytecode = this.toBytecode();
        return null;
    }
}
