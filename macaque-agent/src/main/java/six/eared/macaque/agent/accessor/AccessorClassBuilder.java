package six.eared.macaque.agent.accessor;

import io.github.hhy50.linker.asm.AsmClassBuilder;
import io.github.hhy50.linker.asm.MethodBuilder;
import io.github.hhy50.linker.define.MethodDescriptor;
import io.github.hhy50.linker.generate.MethodBody;
import io.github.hhy50.linker.generate.bytecode.action.Actions;
import io.github.hhy50.linker.generate.bytecode.action.LdcLoadAction;
import io.github.hhy50.linker.generate.bytecode.action.MethodInvokeAction;
import io.github.hhy50.linker.generate.bytecode.utils.Args;
import io.github.hhy50.linker.generate.bytecode.utils.Members;
import io.github.hhy50.linker.generate.bytecode.utils.Methods;
import io.github.hhy50.linker.generate.bytecode.vars.ObjectVar;
import lombok.Getter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import six.eared.macaque.agent.asm2.*;
import six.eared.macaque.common.util.Maps;

import java.util.HashMap;
import java.util.Map;


public class AccessorClassBuilder extends AsmClassBuilder {

    private static final String FIELD_GETTER_ANNO = "Lio/github/hhy50/linker/annotations/Field$Getter;";
    private static final String FIELD_SETTER_ANNO = "Lio/github/hhy50/linker/annotations/Field$Setter;";
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
        this.linkerClassBuilder = new AsmClassBuilder(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE, className+"$Linker", null, null, null);

        Type linkerType = AsmUtil.getType(linkerClassBuilder.getClassName());
        this.defineField(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, STATIC_LINKER_FIELD_NAME, linkerType, null, null)
                .store(getClinit(), new MethodInvokeAction(MethodDescriptor.LINKER_FACTORY_CREATE_LINKER).setArgs(
                        LdcLoadAction.of(linkerType),
                        new MethodInvokeAction(MethodDescriptor.GET_CLASS_LOADER).setInstance(LdcLoadAction.of(AsmUtil.getType(className)))
                ));
        this.defineField(Opcodes.ACC_PRIVATE, LINKER_FIELD_NAME, linkerType, null, null);
    }

    public void defineThis$0() {
        this.defineField(Opcodes.ACC_PUBLIC, "this$0", ObjectVar.TYPE, null, null);
    }

    public AccessorClassBuilder setThis$0(String this$0) {
        this.this$0 = this$0;
        this.linkerClassBuilder.addAnnotation(TARGET_BIND_ANNO, Maps.of("value", this$0));
        return this;
    }

    public AccessorClassBuilder setParent(Accessor parent) {
        this.parent = parent;
        // 创建构造函数
        this.defineConstruct(Opcodes.ACC_PUBLIC, Object.class)
                .intercept(Methods.invokeSuper(MethodDescriptor.ofConstructor())
                        .andThen(Members.ofStore("this$0", Args.of(0)))
                        .andThen(Members.ofStore(LINKER_FIELD_NAME, new MethodInvokeAction(MethodDescriptor.LINKER_FACTORY_CREATE_LINKER)
                                .setArgs(LdcLoadAction.of(AsmUtil.getType(linkerClassBuilder.getClassName())), Args.of(0))))
                        .andThen(Actions.areturn(Type.VOID_TYPE)));
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
            rule = invokeInstance(owner, methodName, method);
        }
        this.methodAccessRules.put(ClassMethodUniqueDesc.of(owner, method.getMethodName(), method.getDesc()), rule);
    }

    private MethodAccessRule invokeStatic(String owner, String methodName, AsmMethod method) {
        if (method.isPrivate()) {
            Type linkerType = AsmUtil.getType(linkerClassBuilder.getClassName());
            super.defineMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, methodName, method.getDesc(), method.getExceptions())
                    .intercept(new MethodInvokeAction(MethodDescriptor.of(linkerType.getInternalName(), methodName, method.getDesc()))
                            .setInstance(Members.ofStatic(STATIC_LINKER_FIELD_NAME, linkerType))
                            .setArgs(Args.loadArgs())
                            .thenReturn()
                    );
            return MethodAccessRule.forward(true, this.getClassName(), methodName, method.getDesc());
        }
        return MethodAccessRule.direct();
    }

    private MethodAccessRule invokeInstance(String owner, String methodName, AsmMethod method) {
        Type linkerType = AsmUtil.getType(linkerClassBuilder.getClassName());
        super.defineMethod(Opcodes.ACC_PUBLIC, methodName, method.getDesc(), method.getExceptions())
                .intercept(new MethodInvokeAction(MethodDescriptor.of(linkerType.getInternalName(), methodName, method.getDesc()))
                        .setInstance(Members.ofLoad(LINKER_FIELD_NAME))
                        .setArgs(Args.loadArgs())
                        .thenReturn()
                );
        return MethodAccessRule.forward(true, this.getClassName(), methodName, method.getDesc());
    }

    public void addField(String owner, AsmField filed) {
        String fullName = owner.replace('.', '_')+"_"+filed.getFieldName();
        linkerClassBuilder.defineMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, fullName, "()"+filed.getDesc(), null)
                .addAnnotation(FIELD_GETTER_ANNO, Maps.of("value", filed.getFieldName()));
        linkerClassBuilder.defineMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, fullName, "("+filed.getDesc()+")V", null)
                .addAnnotation(FIELD_SETTER_ANNO, Maps.of("value", filed.getFieldName()));

        String getter = generateGetter(className, fullName, filed);
        String setter = generateSetter(className, fullName, filed);
        if (getter == null && setter == null) {

        } else {
            this.fieldAccessRules.put(ClassFieldUniqueDesc.of(owner, filed.getFieldName(), filed.getDesc()),
                    FieldAccessRule.forwardToMethod(filed.isStatic(), this.getClassName(), getter, setter));
        }
    }


    /**
     * 为私有字段和实例字段生成访问方法
     * 排除非私有的静态(ps: 非私有的静态可以在任意地方访问,所以不需要访问方法)
     * @param owner
     * @param getterName
     * @param asmField
     * @return
     */
    private String generateGetter(String owner, String getterName, AsmField asmField) {
        String getter = Accessor.FIELD_GETTER_PREFIX+getterName;
        Type linkerType = AsmUtil.getType(linkerClassBuilder.getClassName());
        String methodDesc = "()"+asmField.getDesc();
        super.defineMethod(Opcodes.ACC_PUBLIC, getter, methodDesc, null)
                .intercept(new MethodInvokeAction(MethodDescriptor.of(linkerType.getInternalName(), getterName, methodDesc))
                        .setInstance(Members.ofLoad(asmField.isStatic()?STATIC_LINKER_FIELD_NAME:LINKER_FIELD_NAME))
                        .setArgs(Args.loadArgs())
                        .thenReturn()
                );
        return getter;
    }

    private String generateSetter(String owner, String setterName, AsmField asmField) {
        String setter = Accessor.FIELD_SETTER_PREFIX+setterName;
        Type linkerType = AsmUtil.getType(linkerClassBuilder.getClassName());
        String methodDesc = "("+asmField.getDesc()+")V";
        super.defineMethod(Opcodes.ACC_PUBLIC, setter, methodDesc, null)
                .intercept(new MethodInvokeAction(MethodDescriptor.of(linkerType.getInternalName(), setterName, methodDesc))
                        .setInstance(Members.ofLoad(asmField.isStatic()?STATIC_LINKER_FIELD_NAME:LINKER_FIELD_NAME))
                        .setArgs(Args.loadArgs())
                        .thenReturn()
                );
        return setter;
    }

    public Accessor toAccessor() {
        Accessor accessor = new Accessor();
        accessor.parent = parent;
        accessor.fieldAccessRules = fieldAccessRules;
        accessor.methodAccessRules = methodAccessRules;
        accessor.definition = AsmUtil.readClass(this.toBytecode());
        return accessor;
    }
}
