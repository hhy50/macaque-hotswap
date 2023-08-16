package six.eared.macaque.agent.asm2.classes;

import six.eared.macaque.asm.ClassReader;

import java.util.Iterator;

public class MultiClassReader implements Iterable<ClazzDefinition> {
    protected byte[] multiClassData;

    protected volatile int pos = 0;

    public MultiClassReader(byte[] multiClassData) {
        this.multiClassData = multiClassData;
    }

    @Override
    public Iterator<ClazzDefinition> iterator() {
        return new MultiClassReaderItr();
    }

    class MultiClassReaderItr implements Iterator<ClazzDefinition> {

        ClazzDefinitionVisitor visitor = new ClazzDefinitionVisitor();

        @Override
        public boolean hasNext() {
            return pos < multiClassData.length;
        }

        @Override
        public ClazzDefinition next() {
            ClassReader classReader = new ClassReader(multiClassData, pos);
            pos = classReader.accept(visitor, 0);

            return visitor.getDefinition();
        }
    }
}
