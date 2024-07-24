package six.eared.macaque.agent.accessor;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import javassist.bytecode.Bytecode;
import lombok.Setter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.javassist.JavassistClassBuilder;

import java.util.Arrays;
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
        AccessorRule rule = null;
        if (method.isStatic()) {
            rule = invokerStatic(owner, method);
        } else if (method.isPrivate()) {
            rule = invokeSpecial(owner, method);
        } else {
            // 继承而来 （如果自己重写了父类的方法, 就保存父类的字节码，防止 super调用）
            if (!owner.equals(this.this$0) && parent != null) {
               return;
            }
            // 不是继承而来的 或者 继承来的但是没有父accessor, 就生成方法调用
            rule = invokerVirtual(owner, method);
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

    /**
     *
     */
    private AccessorRule invokerStatic(String owner, AsmMethod method) throws CannotCompileException {
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
                    .defineMethod("public static "+rType+" "+methodName+ "("+argsVarDeclare+") { throw new RuntimeException(\"not impl\"); }", (bytecode) -> {
                        // return (rType) mh.invoke(args...);
                        bytecode.addGetstatic(owner, mhVar, "Ljava/lang/invoke/MethodHandle;");
                        loadArgs(bytecode, args);
                        bytecode.addInvokevirtual("java/lang/invoke/MethodHandle", "invoke", methodType.getDescriptor());
                        areturn(bytecode, methodType.getReturnType());

                        int lvb = AsmUtil.calculateLvbOffset(true, args);
                        bytecode.setMaxLocals(lvb);
                        bytecode.setMaxStack(2+lvb); // 2=mh+?
                    });
            return AccessorRule.forward(true, this.getClassName(), methodName, method.getDesc());
        }
        return AccessorRule.direct();
    }

    private AccessorRule invokerVirtual(String owner, AsmMethod method) throws CannotCompileException {
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
        return AccessorRule.forward(true, this.getClassName(), methodName, method.getDesc());
    }


    private AccessorRule invokeSpecial(String owner, AsmMethod method) throws CannotCompileException {
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
        return AccessorRule.forward(true, this.getClassName(), newMethodName, method.getDesc());
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
        bytecode.addGetfield(this$0Holder, "this$0", AsmUtil.toTypeDesc(this0Class));
    }

    private static void areturn(Bytecode bytecode, Type rType) {
        bytecode.add(rType.getOpcode(Opcodes.IRETURN));
    }
}
