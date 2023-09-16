package six.eared.macaque.agent.asm2.classes;

import six.eared.macaque.asm.ClassReader;

import java.util.Iterator;

public class MultiClassReader implements Iterable<ClazzDefinition> {
    private ClazzDefinitionVisitorFactory visitorFactory;

    protected byte[] multiClassData;

    protected volatile int pos = 0;

    public MultiClassReader(byte[] multiClassData, ClazzDefinitionVisitorFactory visitorFactory) {
        this.multiClassData = multiClassData;
        this.visitorFactory = visitorFactory;
    }

    public MultiClassReader(byte[] multiClassData) {
        this(multiClassData, new ClazzDefinitionVisitorFactory.Default());
    }

    @Override
    public Iterator<ClazzDefinition> iterator() {
        return new MultiClassReaderItr();
    }

    class MultiClassReaderItr implements Iterator<ClazzDefinition> {

        @Override
        public boolean hasNext() {
            return pos < multiClassData.length;
        }

        @Override
        public ClazzDefinition next() {
            ClazzDefinitionVisitor visitor = visitorFactory.creatClazzVisitor();
            ClassReader classReader = new ClassReader(multiClassData, pos);
            pos = classReader.accept(visitor, 0);

            return visitor.getDefinition();
        }
    }
}
