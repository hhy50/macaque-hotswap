package six.eared.macaque.agent.test;


public class StaticEarlyClass extends AbsEarlyClass2 {

    public String test1() {
        return "test1";
    }

    public String test2() {
        return "test2";
    }

    /**
     * index=21, visitTypeInsn(), opcode='187', type='java/lang/NoSuchMethodError'
     * index=22, visitInsn(), opcode='89'
     * index=23, visitLdcInsn(), cst='1234566'
     * index=24, visitMethodInsn(), opcode='183', owner='java/lang/NoSuchMethodError', name='<init>', desc='(Ljava/lang/String;)V', itf='false'
     * index=25, visitInsn(), opcode='191'
     * index=26, visitMaxs(), maxStack='3', maxLocals='0'
     *
     * @return
     */
    public static String staticMethod1() {
        return "staticMethod1";
    }
}
