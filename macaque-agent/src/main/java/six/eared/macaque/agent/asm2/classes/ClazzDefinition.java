package six.eared.macaque.agent.asm2.classes;

import lombok.Data;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import six.eared.macaque.agent.asm2.AsmField;
import six.eared.macaque.agent.asm2.AsmMethod;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.objectweb.asm.Opcodes.*;

@Data
public abstract class ClazzDefinition implements Cloneable {
    protected int classVersion;

    protected int modifiers;

    protected String className;

    protected String superClassName;

    protected String[] interfaces;

    private String sign;

    protected final List<AsmMethod> asmMethods = new ArrayList<>();

    protected final List<AsmField> asmFields = new ArrayList<>();

    public void addAsmMethod(AsmMethod method) {
        this.asmMethods.add(method);
    }

    public void addAsmField(AsmField asmField) {
        this.asmFields.add(asmField);
    }

    @Override
    public ClazzDefinition clone() {
        try {
            return (ClazzDefinition) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasMethod(String name, String desc) {
        return asmMethods.stream()
                .anyMatch(item -> item.getMethodName().equals(name) && item.getDesc().equals(desc));
    }

    public AsmMethod getMethod(String name, String desc) {
        return asmMethods.stream()
                .filter(item -> item.getMethodName().equals(name) && item.getDesc().equals(desc))
                .findAny().orElse(null);
    }

    public AsmField getField(String name, String desc) {
        return asmFields.stream()
                .filter(item -> item.getFieldName().equals(name) && item.getDesc().equals(desc))
                .findAny().orElse(null);
    }

    public boolean hasMethod(AsmMethod asmMethod) {
        AsmMethod method = getMethod(asmMethod.getMethodName(), asmMethod.getDesc());
        return method != null && method.isStatic() == asmMethod.isStatic();
    }

    public static class InMemory extends ClazzDefinition {

        public InMemory(Class<?> clazz) {
            this.classVersion = Opcodes.V1_8;
            this.modifiers = toAsmOpcode(clazz.getModifiers());
            this.className = clazz.getName();
            this.superClassName = Optional.ofNullable(clazz.getSuperclass()).map(Class::getName).orElse(null);
            this.interfaces = Arrays.stream(clazz.getInterfaces()).map(Class::getName).toArray(String[]::new);

            for (Method method : clazz.getDeclaredMethods()) {
                AsmMethod asmMethod = AsmMethod.AsmMethodBuilder
                        .builder()
                        .modifier(toAsmOpcode(method.getModifiers()))
                        .methodName(method.getName())
                        .desc(Type.getMethodDescriptor(method))
                        .build();
                addAsmMethod(asmMethod);
            }
            for (Constructor<?> constructor : clazz.getConstructors()) {
                AsmMethod asmMethod = AsmMethod.AsmMethodBuilder
                        .builder()
                        .modifier(toAsmOpcode(constructor.getModifiers()))
                        .methodName("<init>")
                        .desc("()V")
                        .build();
                addAsmMethod(asmMethod);
            }

            boolean hasStatic = false;
            for (Field field : clazz.getDeclaredFields()) {
                AsmField asmField = AsmField.AsmFieldBuilder
                        .builder()
                        .modifier(toAsmOpcode(field.getModifiers()))
                        .fieldName(field.getName())
                        .fieldDesc(Type.getDescriptor(field.getType()))
                        .build();
                addAsmField(asmField);
                hasStatic = hasStatic | (asmField.getModifier() | ACC_STATIC) > 0;
            }

            if (hasStatic) {
                addAsmMethod(AsmMethod.AsmMethodBuilder
                    .builder()
                    .modifier(toAsmOpcode(ACC_STATIC))
                    .methodName("<clinit>")
                    .desc("()V")
                    .build());
            }

//            AsmField asmField = AsmField.AsmFieldBuilder
//                    .builder()
//                    .modifier(toAsmOpcode(field.getModifiers()))
//                    .fieldName(field.getName())
//                    .fieldDesc(Type.getDescriptor(field.getType()))
//                    .build();
//            addAsmField(asmField);
        }

        public static int toAsmOpcode(int modifier) {
            int asmOpcode = 0;
            if ((modifier & Modifier.PUBLIC) != 0) asmOpcode |= ACC_PUBLIC;
            if ((modifier & Modifier.PRIVATE) != 0) asmOpcode |= ACC_PRIVATE;
            if ((modifier & Modifier.PROTECTED) != 0) asmOpcode |= ACC_PROTECTED;
            if ((modifier & Modifier.STATIC) != 0) asmOpcode |= ACC_STATIC;
            if ((modifier & Modifier.FINAL) != 0) asmOpcode |= ACC_FINAL;
            if ((modifier & Modifier.SYNCHRONIZED) != 0) asmOpcode |= ACC_SYNCHRONIZED;
            if ((modifier & Modifier.VOLATILE) != 0) asmOpcode |= ACC_VOLATILE;
            if ((modifier & Modifier.TRANSIENT) != 0) asmOpcode |= ACC_TRANSIENT;
            if ((modifier & Modifier.NATIVE) != 0) asmOpcode |= ACC_NATIVE;
            if ((modifier & Modifier.INTERFACE) != 0) asmOpcode |= ACC_INTERFACE;
            if ((modifier & Modifier.ABSTRACT) != 0) asmOpcode |= ACC_ABSTRACT;
            if ((modifier & Modifier.STRICT) != 0) asmOpcode |= ACC_STRICT;
            return asmOpcode;
        }
    }
}
