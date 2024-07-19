package six.eared.macaque.agent.asm2.classes;

public class ClassReader extends org.objectweb.asm.ClassReader {

    public ClassReader(byte[] classFile, int start) {
        super(classFile, start, classFile.length);
    }



}
