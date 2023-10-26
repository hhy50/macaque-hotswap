package six.eared.macaque.agent.test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;

public class EarlyClass extends AbsEarlyClass2 {
    private MethodHandles.Lookup lookup;

    public String test1() {
        System.out.println("test1");
        return "test1";
    }

    public String test2() {
        System.out.println("test2");
        return "test2";
    }

    public String test3() {
        return "test3";
    }

    public String test5() {
        return "test5";
    }

    public void test6() throws NoSuchMethodException, IllegalAccessException {
        MethodType type = MethodType.methodType(void.class, byte.class, short.class, int.class, long.class, float.class, double.class, char.class, boolean.class);
        MethodHandle mh = lookup
                .findSpecial(String.class,"test1", type, void. class);
    }

    public class Macaque_Accessor {
        protected MethodHandles.Lookup lookup;

        public Macaque_Accessor() {
            try {
                Constructor<?> constructor = MethodHandles.Lookup.class.getConstructors()[0];
                constructor.setAccessible(true);
                lookup = (MethodHandles.Lookup) MethodHandles.Lookup.class.getConstructors()[0].newInstance(EarlyClass.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public String super_test1(EarlyClass arg1, AbsEarlyClass arg2) {
            MethodType type = MethodType.methodType(EarlyClass.class, Parameter.class, String.class);
            try {
                MethodHandle mh = lookup
                        .findSpecial(AbsEarlyClass.class, "test1", type, EarlyClass.class)
                        .bindTo(EarlyClass.this);
                return (String) mh.invoke(arg1, arg2);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
}
