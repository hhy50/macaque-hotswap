package six.eared.macaque.agent.hotswap;

import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.env.Environment;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.Arrays;
import java.util.Map;

public class ClassHotSwapper {


    public static int redefines(Map<String, byte[]> definitions) throws UnmodifiableClassException, ClassNotFoundException {
        if (Environment.isDebug()) {
            System.out.printf("[ClassHotSwap.process] redefines class count: [%d]%n", definitions.size());
        }

        Instrumentation inst = Environment.getInst();
        ClassDefinition[] classDefinitions = Arrays.stream(inst.getAllLoadedClasses())
                .filter(item -> definitions.containsKey(item.getName()))
                .map(clazz -> new ClassDefinition(clazz, definitions.get(clazz.getName())))
                .toArray(ClassDefinition[]::new);
        inst.redefineClasses(classDefinitions);
        return classDefinitions.length;
    }
}
