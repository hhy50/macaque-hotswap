package six.eared.macaque.agent.asm.classes;

import org.objectweb.asm.ClassReader;

import java.util.Iterator;

public class MultiClassReader extends ClassReader implements Iterable<ClazzDefinition> {

    public MultiClassReader(byte[] multiClassData) {
        super(multiClassData);
    }


    @Override
    public Iterator<ClazzDefinition> iterator() {
        return new MultiClassReaderItr(this);
    }


    static class MultiClassReaderItr implements Iterator<ClazzDefinition> {

        public MultiClassReaderItr(MultiClassReader classDefinitions) {

        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public ClazzDefinition next() {
            return null;
        }
    }
}
