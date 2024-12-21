package six.eared.macaque.mybatis;

import java.lang.invoke.*;

public class StrictMapBootstrap {

    public static CallSite bootstrap(MethodHandles.Lookup lookup, String name, MethodType type)
            throws NoSuchMethodException, IllegalAccessException, ClassNotFoundException {
        Class<?> strictMap = lookup.lookupClass();

        MethodHandle methodHandle = lookup.findSpecial(
                strictMap.getSuperclass(),
                "put",
                MethodType.methodType(Object.class, Object.class, Object.class),
                strictMap);
        return new ConstantCallSite(methodHandle);
    }
}
