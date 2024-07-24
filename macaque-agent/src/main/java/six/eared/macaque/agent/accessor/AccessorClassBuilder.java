package six.eared.macaque.agent.accessor;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import javassist.bytecode.Bytecode;
import lombok.Setter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import six.eared.macaque.agent.asm2.AsmField;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.MethodUniqueDesc;
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

    Map<MethodUniqueDesc, MethodAccessorRule> methodAccessorRules = new HashMap<>();

    @Setter
    private Accessor parent;

    public AccessorClassBuilder(int modifier, String className, String superClass, String[] interfaces) throws NotFoundException, CannotCompileException {
        super(modifier, className, superClass, interfaces);
    }

    public static AccessorClassBuilder builder(int modifier, String className, String superClass, String[] interfaces)
            throws NotFoundException, CannotCompileException {
        return new AccessorClassBuilder(modifier, className, superClass, interfaces);
    }

    public void addMethod(String owner, AsmMethod method) throws CannotCompileException {
        MethodAccessorRule rule = null;
        if (method.isStatic()) {
            rule = invokerStatic(owner, method);
        } else if (method.isPrivate()) {
            rule = invokeSpecial(owner, method);
        } else if (owner.equals(this.this$0)) { // 不是继承而来的
            rule = invokerVirtual(owner, method);
        } else if (parent == null) { // 继承来的但是没有父accessor, 就生成方法调用
            rule = invokeSpecial(owner, method);
        }
        this.methodAccessorRules.put(MethodUniqueDesc.of(method.getMethodName(), method.getDesc()), rule);
    }

    public void addField(String owner, AsmField filed) throws CannotCompileException {
        getField(owner, filed);
        if (filed.isFinal()) {
            setField(owner, filed);
        }
    }

    /**
     *
     */
    private MethodAccessorRule invokerStatic(String owner, AsmMethod method) throws CannotCompileException {
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
                        loadArgs(bytecode, args);
                        bytecode.addInvokevirtual("java/lang/invoke/MethodHandle", "invoke", methodType.getDescriptor());
                        areturn(bytecode, methodType.getReturnType());

                        int lvb = AsmUtil.calculateLvbOffset(true, args);
                        bytecode.setMaxLocals(lvb);
                        bytecode.setMaxStack(2+lvb); // 2=mh+?
                    });
            return MethodAccessorRule.forward(true, this.getClassName(), methodName, method.getDesc());
        }
        return MethodAccessorRule.direct();
    }

    private MethodAccessorRule invokerVirtual(String owner, AsmMethod method) throws CannotCompileException {
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
        return MethodAccessorRule.forward(true, this.getClassName(), methodName, method.getDesc());
    }


    private MethodAccessorRule invokeSpecial(String owner, AsmMethod method) throws CannotCompileException {
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
                    loadArgs(bytecode, args);
                    bytecode.addInvokevirtual("java/lang/invoke/MethodHandle", "invoke", AsmUtil.addArgsDesc(methodType.getDescriptor(), this$0, true));
                    areturn(bytecode, methodType.getReturnType());

                    int lvb = AsmUtil.calculateLvbOffset(false, args);
                    bytecode.setMaxLocals(lvb);
                    bytecode.setMaxStack(2+lvb); // 2=mh+this$0
                });
        return MethodAccessorRule.forward(true, this.getClassName(), newMethodName, method.getDesc());
    }

    /**
     * 为私有字段和实例字段生成访问方法
     * 排除非私有的静态(ps: 非私有的静态可以在任意地方访问,所以不需要访问方法)
     *
     * @param asmField
     * @throws CannotCompileException
     */
    private void getField(String owner, AsmField asmField) throws CannotCompileException {
        Type fieldType = Type.getType(asmField.getDesc());
        String type = fieldType.getClassName();
        String name = asmField.getFieldName();

        String declare = "public "+(asmField.isStatic()?"static ":"")+type+" "+(owner.replace('.', '_')+Accessor.FIELD_GETTER_PREFIX+name)+"()";
        if (asmField.isPrivate()) {
            String mhVar = name+"_getter_mh_"+COUNTER.getAndIncrement();
            super.defineField("private static final MethodHandle "+mhVar+"=LOOKUP."+(asmField.isStatic()?"findStaticGetter":"findGetter")+"("+owner+".class, \""+name+"\", "+fieldType.getClassName()+".class);")
                    .defineMethod(declare+"{ throw new RuntimeException(\"not impl\"); }", (bytecode) -> {
                        String dynamicDesc = "()"+asmField.getDesc();
                        if (!asmField.isStatic()) {
                            loadThis$0(bytecode, getThis$0holder(), this$0);
                            dynamicDesc = AsmUtil.addArgsDesc(dynamicDesc, this$0, true);
                        }
                        // mh.invoke(this$0, args...)
                        bytecode.addGetstatic(this.getClassName(), mhVar, "Ljava/lang/invoke/MethodHandle;");
                        bytecode.addInvokevirtual("java/lang/invoke/MethodHandle", "invoke", dynamicDesc);
                        areturn(bytecode, fieldType);

                        bytecode.setMaxLocals(asmField.isStatic()?0:1); // 1=this
                        bytecode.setMaxStack(asmField.isStatic()?1:2); // 2=mh+this$0, 静态没有this$0
                    });
        } else if (!asmField.isStatic()) {
            super.defineMethod(declare+"{ return "+("(("+this$0+") this$0)."+name)+"; }");
        }
    }

    /**
     * 为私有字段和实例字段生成访问方法
     * 排除非私有的静态(ps: 非私有的静态可以在任意地方访问,所以不需要访问方法)
     *
     * @param owner
     * @param asmField
     * @throws CannotCompileException
     */
    private void setField(String owner, AsmField asmField) throws CannotCompileException {
        Type fieldType = Type.getType(asmField.getDesc());
        String type = fieldType.getClassName();
        String name = asmField.getFieldName();

        String declare = "public "+(asmField.isStatic()?"static ":"")+"void "+(owner.replace('.', '_')+Accessor.FIELD_SETTER_PREFIX+name)+"("+type+" arg)";
        if (asmField.isPrivate()) {
            String mhVar = name+"_set_mh_"+COUNTER.getAndIncrement();
            super.defineField("private static final MethodHandle "+mhVar+"=LOOKUP."+(asmField.isStatic()?"findStaticSetter":"findSetter")+"("+owner+".class, \""+name+"\", "+fieldType.getClassName()+".class);")
                    .defineMethod(declare+"{ throw new RuntimeException(\"not impl\"); }", (bytecode) -> {
                        String dynamicDesc = "("+asmField.getDesc()+")V";
                        if (!asmField.isStatic()) {
                            loadThis$0(bytecode, getThis$0holder(), this$0);
                            dynamicDesc = AsmUtil.addArgsDesc(dynamicDesc, this$0, true);
                        }
                        bytecode.addGetstatic(this.getClassName(), mhVar, "Ljava/lang/invoke/MethodHandle;");
                        bytecode.addInvokevirtual("java/lang/invoke/MethodHandle", "invoke", dynamicDesc);
                        areturn(bytecode, fieldType);

                        int lvb = AsmUtil.calculateLvbOffset(asmField.isStatic(), new Type[]{fieldType});
                        bytecode.setMaxLocals(asmField.isStatic()?lvb:lvb+1);
                        bytecode.setMaxLocals(asmField.isStatic()?lvb:lvb+1);
                    });
        } else if (!asmField.isStatic()) {
            super.defineMethod(declare+"{"+this$0+"."+name+"=arg;}");
        }
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

    private static void loadArgs(Bytecode bytecode, Type[] argumentTypes) {
        int i = 0;
        for (Type argumentType : argumentTypes) {
            bytecode.add(argumentType.getOpcode(Opcodes.ILOAD), i+1);
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
        bytecode.add(rType.getOpcode(Opcodes.IRETURN));
    }

    public Accessor toAccessor() {
        Accessor accessor = new Accessor();
        accessor.ownerClass = this$0;
        accessor.methodAccessorRules = methodAccessorRules;
        accessor.parent = parent;
        accessor.definition = AsmUtil.readClass(this.toByteArray());
        return accessor;
    }
}
