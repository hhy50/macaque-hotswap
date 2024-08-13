package six.eared.macaque.agent.asm2.classes;

import org.objectweb.asm.ClassReader;
import six.eared.macaque.agent.asm2.ClassReaderUtil;
import six.eared.macaque.agent.enhance.ClazzDataDefinition;

import java.util.Arrays;
import java.util.Iterator;

public class MultiClassReader implements Iterable<ClazzDataDefinition> {
    private ClazzDefinitionVisitorFactory visitorFactory;

    protected byte[] multiClassData;

    protected volatile int pos = 0;

    public MultiClassReader(byte[] multiClassData, ClazzDefinitionVisitorFactory visitorFactory) {
        this.multiClassData = multiClassData;
        this.visitorFactory = visitorFactory;
    }

    @Override
    public Iterator<ClazzDataDefinition> iterator() {
        return new MultiClassReaderItr();
    }

    class MultiClassReaderItr implements Iterator<ClazzDataDefinition> {

        private final ClazzDefinitionVisitor visitor = new ClazzDefinitionVisitor();

        @Override
        public boolean hasNext() {
            return pos < multiClassData.length;
        }

        @Override
        public ClazzDataDefinition next() {
            ClassReader classReader = new ClassReader(multiClassData, pos, multiClassData.length);
            classReader.accept(visitor, 0);

            int endOffset = ClassReaderUtil.getEndOffset(classReader);
            visitor.setByteCode(Arrays.copyOfRange(multiClassData, pos, endOffset));
            pos = endOffset;
            return visitor.getDefinition();
        }
    }
}
