//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package six.eared.macaque.agent.test.case1;

import six.eared.macaque.agent.accessor.util.Util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class TestAddMethodClass$macaque$Accessor {
    public Object this$0;
    public static final MethodHandles.Lookup LOOKUP = Util.lookup(TestAddMethodClass.class);
    private static  MethodHandle test3_mh_1;
    private static  MethodHandle getClass_mh_2;
    private static  MethodHandle hashCode_mh_3;
    private static  MethodHandle equals_mh_4;
    private static  MethodHandle clone_mh_5;
    private static  MethodHandle toString_mh_6;
    private static  MethodHandle notify_mh_7;
    private static  MethodHandle notifyAll_mh_8;
    private static  MethodHandle wait_mh_9;
    private static  MethodHandle wait_mh_10;
    private static  MethodHandle wait_mh_11;
    private static  MethodHandle finalize_mh_12;

    public TestAddMethodClass$macaque$Accessor(Object var1) {
        this.this$0 = var1;
    }

    public String test2() {
        return (String)((TestAddMethodClass)this.this$0).test2();
    }

    public String test1() {
        return (String)((TestAddMethodClass)this.this$0).test1();
    }

    public static String test3() throws Throwable {
        return (String) test3_mh_1.invoke();
    }

    public Object six_eared_macaque_agent_test_TestAddMethodClass$macaque$get$field$field1() {
        return ((TestAddMethodClass)this.this$0).field1;
    }

    public Class java_lang_Object_getClass() throws Throwable {
        return (Class) getClass_mh_2.invoke((TestAddMethodClass)this.this$0);
    }

    public int java_lang_Object_hashCode() throws Throwable {
        return (int) hashCode_mh_3.invoke((TestAddMethodClass)this.this$0);
    }

    public boolean java_lang_Object_equals(Object var1) throws Throwable {
        return (boolean) equals_mh_4.invoke((TestAddMethodClass)this.this$0, var1);
    }

    public Object java_lang_Object_clone() throws Throwable {
        return clone_mh_5.invoke((TestAddMethodClass)this.this$0);
    }

    public String java_lang_Object_toString() throws Throwable {
        return (String) toString_mh_6.invoke((TestAddMethodClass)this.this$0);
    }

    static {
        try {
            test3_mh_1 = LOOKUP.findStatic(TestAddMethodClass.class, "test3", MethodType.methodType(String.class, new Class[0]));
            getClass_mh_2 = LOOKUP.findSpecial(Object.class, "getClass", MethodType.methodType(Class.class, new Class[0]), TestAddMethodClass.class);
            hashCode_mh_3 = LOOKUP.findSpecial(Object.class, "hashCode", MethodType.methodType(Integer.TYPE, new Class[0]), TestAddMethodClass.class);
            equals_mh_4 = LOOKUP.findSpecial(Object.class, "equals", MethodType.methodType(Boolean.TYPE, new Class[]{Object.class}), TestAddMethodClass.class);
            clone_mh_5 = LOOKUP.findSpecial(Object.class, "clone", MethodType.methodType(Object.class, new Class[0]), TestAddMethodClass.class);
            toString_mh_6 = LOOKUP.findSpecial(Object.class, "toString", MethodType.methodType(String.class, new Class[0]), TestAddMethodClass.class);
            notify_mh_7 = LOOKUP.findSpecial(Object.class, "notify", MethodType.methodType(Void.TYPE, new Class[0]), TestAddMethodClass.class);
            notifyAll_mh_8 = LOOKUP.findSpecial(Object.class, "notifyAll", MethodType.methodType(Void.TYPE, new Class[0]), TestAddMethodClass.class);
            wait_mh_9 = LOOKUP.findSpecial(Object.class, "wait", MethodType.methodType(Void.TYPE, new Class[]{Long.TYPE}), TestAddMethodClass.class);
            wait_mh_10 = LOOKUP.findSpecial(Object.class, "wait", MethodType.methodType(Void.TYPE, new Class[]{Long.TYPE, Integer.TYPE}), TestAddMethodClass.class);
            wait_mh_11 = LOOKUP.findSpecial(Object.class, "wait", MethodType.methodType(Void.TYPE, new Class[0]), TestAddMethodClass.class);
            finalize_mh_12 = LOOKUP.findSpecial(Object.class, "finalize", MethodType.methodType(Void.TYPE, new Class[0]), TestAddMethodClass.class);
        } catch (Throwable e) {

        }
    }

    public static void main(String[] args) throws Throwable {
        TestAddMethodClass testAddMethodClass = new TestAddMethodClass();
        TestAddMethodClass$macaque$Accessor accessor = new TestAddMethodClass$macaque$Accessor(testAddMethodClass);
        System.out.println(accessor.java_lang_Object_equals(testAddMethodClass));
        System.out.println(accessor.java_lang_Object_getClass()==testAddMethodClass.getClass());
    }
}
