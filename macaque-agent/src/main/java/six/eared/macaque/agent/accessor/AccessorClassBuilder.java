package six.eared.macaque.agent.accessor;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.Opcode;
import lombok.Setter;
import org.objectweb.asm.Type;
import six.eared.macaque.agent.asm2.*;
import six.eared.macaque.agent.javassist.JavassistClassBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class AccessorClassBuilder extends JavassistClassBuilder {

    private static final AtomicInteger COUNTER = new AtomicInteger(1);

    /**
     * owner class
     */
    @Setter
    private String this$0;

    Map<ClassMethodUniqueDesc, MethodAccessRule> methodAccessRules = new HashMap<>();
    Map<ClassFieldUniqueDesc, FieldAccessRule> fieldAccessRules = new HashMap<>();

    @Setter
    private Accessor parent;

    public AccessorClassBuilder(int modifier, String className, String superClass, String[] interfaces) throws NotFoundException, CannotCompileException {
        super(modifier, className, superClass, interfaces);
    }

    public static AccessorClassBuilder builder(int modifier, String className, String superClass, String[] interfaces)
            throws NotFoundException, CannotCompileException {
        return new AccessorClassBuilder(modifier, className, superClass, interfaces);
    }

    public void addMethod(String owner, AsmMethod method) throws CannotCompileException, BadBytecode, NotFoundException {
        MethodAccessRule rule = null;
        if (method.isStatic()) {
            rule = invokerStatic(owner, method);
        } else if (method.isPrivate()) {
            rule = invokeSpecial(owner, method);
        } else if (owner.equals(this.this$0)) { // 自己的方法, 不是继承而来的
            rule = invokerVirtual(owner, method);
        } else if (parent == null) { // 继承来的但是没有父accessor, 就生成方法调用
            rule = invokeSpecial(owner, method);
        }
        this.methodAccessRules.put(ClassMethodUniqueDesc.of(owner, method.getMethodName(), method.getDesc()), rule);
    }

    public void addField(String owner, AsmField filed) throws CannotCompileException, BadBytecode, NotFoundException {
        String getter = getField(owner, filed);
        String setter = setField(owner, filed);
        if (getter == null && setter == null) {

        } else {
            this.fieldAccessRules.put(ClassFieldUniqueDesc.of(owner, filed.getFieldName(), filed.getDesc()),
                    FieldAccessRule.forwardToMethod(filed.isStatic(), this.getClassName(), getter, setter));
        }
    }

    /**
     *
     */
    private MethodAccessRule invokerStatic(String owner, AsmMethod method) throws CannotCompileException, BadBytecode, NotFoundException {
        if (method.isPrivate()) {
            String methodName = method.getMethodName();
            Type methodType = Type.getMethodType(method.getDesc());
            String rType = methodType.getReturnType().getClassName();
            Type[] args = methodType.getArgumentTypes();
            String[] argVars = IntStream.range(0, args.length).mapToObj(i -> "var_"+i).toArray(String[]::new);
            String mhVar = methodName+"_mh_"+COUNTER.getAndIncrement();
            String argsTypeDeclare = Arrays.stream(args).map(type -> type.getClassName()+".class").collect(Collectors.joining(","));
            String argsVarDeclare = IntStream.range(0, args.length).mapToObj(i -> args[i].getClassName()+" "+argVars[i]).collect(Collectors.joining(","));

            super.defineField("private static final MethodHandle "+mhVar+" = LOOKUP.findStatic("+this$0+".class,\""+methodName+"\", MethodType.methodType("+rType+".class,new Class[]{"+argsTypeDeclare+"}));")
                    .defineMethod("public static "+rType+" "+methodName+"("+argsVarDeclare+") { throw new RuntimeException(\"not impl\"); }", (bytecode) -> {
                        // return (rType) mh.invoke(args...);
                        bytecode.addGetstatic(this.getClassName(), mhVar, "Ljava/lang/invoke/MethodHandle;");
                        loadArgs(bytecode, 1, args);
                        bytecode.addInvokevirtual("java/lang/invoke/MethodHandle", "invoke", methodType.getDescriptor());
                        areturn(bytecode, methodType.getReturnType());
                    });
            return MethodAccessRule.forward(true, this.getClassName(), methodName, method.getDesc());
        }
        return MethodAccessRule.direct();
    }

    private MethodAccessRule invokerVirtual(String owner, AsmMethod method) throws CannotCompileException {
        String methodName = method.getMethodName();
        Type methodType = Type.getMethodType(method.getDesc());
        String rType = methodType.getReturnType().getClassName();
        Type[] args = methodType.getArgumentTypes();
        String[] argVars = IntStream.range(0, args.length).mapToObj(i -> "var_"+i).toArray(String[]::new);

        String declare = "public "+rType+" "+methodName+"("+
                IntStream.range(0, args.length).mapToObj(i -> args[i].getClassName()+" "+argVars[i]).collect(Collectors.joining(","))+")";
        String body = (rType.equals("void")?"":"return ("+rType+")")+
                "(("+owner+") this$0)."+methodName+"("+String.join(",", argVars)+");";
        this.defineMethod(declare+"{"+body+"}");
        return MethodAccessRule.forward(false, this.getClassName(), methodName, method.getDesc());
    }


    private MethodAccessRule invokeSpecial(String owner, AsmMethod method) throws CannotCompileException, BadBytecode, NotFoundException {
        String methodName = method.getMethodName();
        Type methodType = Type.getMethodType(method.getDesc());
        String rType = methodType.getReturnType().getClassName();
        Type[] args = methodType.getArgumentTypes();
        String[] argVars = IntStream.range(0, args.length).mapToObj(i -> "var_"+i).toArray(String[]::new);
        String argsTypeDeclare = Arrays.stream(args).map(type -> type.getClassName()+".class").collect(Collectors.joining(","));
        String argsVarDeclare = IntStream.range(0, args.length).mapToObj(i -> args[i].getClassName()+" "+argVars[i]).collect(Collectors.joining(","));

        String newMethodName = owner.replace('.', '_')+"_"+methodName;
        String mhVar = methodName+"_mh_"+COUNTER.getAndIncrement();
        super.defineField("private static final MethodHandle "+mhVar+" = LOOKUP.findSpecial("+owner+".class,\""+methodName+"\",MethodType.methodType("+rType+".class,new Class[]{"+argsTypeDeclare+"}), "+this$0+".class);")
                .defineMethod("public "+rType+" "+newMethodName+"("+argsVarDeclare+") { throw new RuntimeException(\"not impl\"); }", (bytecode) -> {
                    // return (rType) mh.invoke(this$0, args...);
                    bytecode.addGetstatic(this.getClassName(), mhVar, "Ljava/lang/invoke/MethodHandle;");
                    loadThis$0(bytecode, getThis$0holder(), this$0);
                    loadArgs(bytecode, 1, args);
                    bytecode.addInvokevirtual("java/lang/invoke/MethodHandle", "invoke", AsmUtil.addArgsDesc(methodType.getDescriptor(), this$0, true));
                    areturn(bytecode, methodType.getReturnType());
                });
        return MethodAccessRule.forward(false, this.getClassName(), newMethodName, method.getDesc());
    }

    /**
     * 为私有字段和实例字段生成访问方法
     * 排除非私有的静态(ps: 非私有的静态可以在任意地方访问,所以不需要访问方法)
     *
     * @param asmField
     * @throws CannotCompileException
     */
    private String getField(String owner, AsmField asmField) throws CannotCompileException, BadBytecode, NotFoundException {
        Type fieldType = Type.getType(asmField.getDesc());
        String type = fieldType.getClassName();
        String name = asmField.getFieldName();

        String getter = owner.replace('.', '_')+Accessor.FIELD_GETTER_PREFIX+name;
        String declare = "public "+(asmField.isStatic()?"static ":"")+type+" "+getter+"()";
        if (asmField.isPrivate()) {
            String mhVar = name+"_getter_mh_"+COUNTER.getAndIncrement();
            super.defineField("private static final MethodHandle "+mhVar+"=LOOKUP."+(asmField.isStatic()?"findStaticGetter":"findGetter")+"("+owner+".class, \""+name+"\", "+fieldType.getClassName()+".class);")
                    .defineMethod(declare+"{ throw new RuntimeException(\"not impl\"); }", (bytecode) -> {
                        String dynamicDesc = "()"+asmField.getDesc();

                        // mh.invoke(this$0?)
                        bytecode.addGetstatic(this.getClassName(), mhVar, "Ljava/lang/invoke/MethodHandle;");
                        if (!asmField.isStatic()) {
                            loadThis$0(bytecode, getThis$0holder(), this$0);
                            dynamicDesc = AsmUtil.addArgsDesc(dynamicDesc, this$0, true);
                        }
                        bytecode.addInvokevirtual("java/lang/invoke/MethodHandle", "invoke", dynamicDesc);
                        areturn(bytecode, fieldType);
                    });
        } else if (!asmField.isStatic()) {
            super.defineMethod(declare+"{ return "+("(("+this$0+") this$0)."+name)+"; }");
        } else {
            return null;
        }
        return getter;
    }

    /**
     * 为私有字段和实例字段生成访问方法
     * 排除非私有的静态(ps: 非私有的静态可以在任意地方访问,所以不需要访问方法)
     *
     * @param owner
     * @param asmField
     * @throws CannotCompileException
     */
    private String setField(String owner, AsmField asmField) throws CannotCompileException, BadBytecode, NotFoundException {
        Type fieldType = Type.getType(asmField.getDesc());
        String type = fieldType.getClassName();
        String name = asmField.getFieldName();

        String setter = owner.replace('.', '_')+Accessor.FIELD_SETTER_PREFIX+name;
        String declare = "public "+(asmField.isStatic()?"static ":"")+"void "+setter+"("+type+" arg)";
        if (asmField.isPrivate()) {
            String mhVar = name+"_set_mh_"+COUNTER.getAndIncrement();
            super.defineField("private static final MethodHandle "+mhVar+"=LOOKUP."+(asmField.isStatic()?"findStaticSetter":"findSetter")+"("+owner+".class, \""+name+"\", "+fieldType.getClassName()+".class);")
                    .defineMethod(declare+"{ throw new RuntimeException(\"not impl\"); }", (bytecode) -> {
                        String dynamicDesc = "("+asmField.getDesc()+")V";

                        // mh.invoke(this$0?, args...)
                        bytecode.addGetstatic(this.getClassName(), mhVar, "Ljava/lang/invoke/MethodHandle;");
                        if (!asmField.isStatic()) {
                            loadThis$0(bytecode, getThis$0holder(), this$0);
                            dynamicDesc = AsmUtil.addArgsDesc(dynamicDesc, this$0, true);
                        }
                        loadArgs(bytecode, 0, new Type[]{fieldType});
                        bytecode.addInvokevirtual("java/lang/invoke/MethodHandle", "invoke", dynamicDesc);
                        areturn(bytecode, Type.VOID_TYPE);
                    });
        } else if (!asmField.isStatic()) {
            super.defineMethod(declare+"{(("+owner+") this$0)."+name+"=arg;}");
        } else {
            return null;
        }
        return setter;
    }

    public String getThis$0holder() {
        if (parent != null) {
            String holder = null;
            Accessor c = parent;
            while (c != null) {
                holder = c.getClassName();
                c = c.parent;
            }
            return holder;
        }
        return super.className;
    }

    /**
     * @param bytecode      字节码
     * @param i             参数起始的局部变量表索引, 静态从0开始, 实例方法从1开始
     * @param argumentTypes 参数
     */
    private static void loadArgs(Bytecode bytecode, int i, Type[] argumentTypes) {
        for (Type argumentType : argumentTypes) {
            bytecode.add(argumentType.getOpcode(Opcode.ILOAD), i);
            if (argumentType.getSort() == Type.DOUBLE || argumentType.getSort() == Type.LONG) i++;
            i++;
        }
    }

    private static void loadThis$0(Bytecode bytecode, String this$0Holder, String this0Class) {
        bytecode.addAload(0);
        bytecode.addGetfield(this$0Holder, "this$0", "Ljava/lang/Object;");
        bytecode.addCheckcast(this0Class);
    }

    private static void areturn(Bytecode bytecode, Type rType) {
        bytecode.add(rType.getOpcode(Opcode.IRETURN));
    }

    public Accessor toAccessor() {
        Accessor accessor = new Accessor();
        accessor.ownerClass = this$0;
        accessor.methodAccessRules = methodAccessRules;
        accessor.fieldAccessRules = fieldAccessRules;
        accessor.parent = parent;
        accessor.definition = AsmUtil.readClass(this.toByteArray());
        return accessor;
    }
}
