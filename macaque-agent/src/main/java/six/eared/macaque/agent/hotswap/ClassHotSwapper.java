package six.eared.macaque.agent.hotswap;

import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.env.Environment;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.ArrayList;
import java.util.List;

public class ClassHotSwapper {

    public static int redefine(ClazzDefinition clazzDefinition) throws UnmodifiableClassException, ClassNotFoundException {
        String className = clazzDefinition.getClassName();
        byte[] newClassData = clazzDefinition.getByteCode();

        Instrumentation inst = Environment.getInst();
        List<ClassDefinition> needRedefineClass = new ArrayList<>();
        for (Class<?> clazz : inst.getAllLoadedClasses()) {
            if (clazz.getName().equals(className)) {
                needRedefineClass.add(new ClassDefinition(clazz, newClassData));
            }
        }
        if (Environment.isDebug()) {
            System.out.printf("[ClassHotSwap.process] className:[%s], find class instance count: [%d]%n", className, needRedefineClass.size());
        }
        ClassDefinition[] array = needRedefineClass.toArray(new ClassDefinition[0]);
        inst.redefineClasses(array);
        return array.length;
    }
}
