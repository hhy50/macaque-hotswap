package six.eared.macaque.agent.hotswap;

import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.env.Environment;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ClassHotSwapper {

    public static int redefine(ClazzDefinition clazzDefinition) throws UnmodifiableClassException, ClassNotFoundException {
        return redefines(Arrays.asList(clazzDefinition));
    }

    public static int redefines(List<ClazzDefinition> definitions) throws UnmodifiableClassException, ClassNotFoundException {
        if (Environment.isDebug()) {
            System.out.printf("[ClassHotSwap.process] redefines class count: [%d]%n", definitions.size());
        }

        Instrumentation inst = Environment.getInst();

        Map<String, ClazzDefinition> definitionMap = definitions.stream()
                .collect(Collectors.toMap(ClazzDefinition::getClassName, Function.identity()));
        ClassDefinition[] classDefinitions = Arrays.stream(inst.getAllLoadedClasses())
                .filter(item -> definitionMap.containsKey(item.getName()))
                .map(clazz -> new ClassDefinition(clazz, definitionMap.get(clazz.getName()).getByteArray()))
                .toArray(ClassDefinition[]::new);
        inst.redefineClasses(classDefinitions);
        return classDefinitions.length;
    }
}
