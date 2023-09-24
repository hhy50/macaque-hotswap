package six.eared.macaque.agent.asm2.classes;

import six.eared.macaque.asm.ClassReader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MultiClassReader implements Iterable<ClazzDefinition> {
    private ClazzDefinitionVisitorFactory visitorFactory;

    protected byte[] multiClassData;

    protected volatile int pos = 0;

    public MultiClassReader(byte[] multiClassData, ClazzDefinitionVisitorFactory visitorFactory) {
        this.multiClassData = multiClassData;
        this.visitorFactory = visitorFactory;
    }

    public MultiClassReader(byte[] multiClassData) {
        this(multiClassData, ClazzDefinitionVisitorFactory.DEFAULT);
    }

    @Override
    public Iterator<ClazzDefinition> iterator() {
        Iterator<ClazzDefinition> lazy = new MultiClassReaderItr();
        if (visitorFactory instanceof ClazzDefinitionVisitorFactory.CompatibilityMode) {
            List<ClazzDefinition> list = new ArrayList<>();
            while (lazy.hasNext()) {
                list.add(lazy.next());
            }
            return list.iterator();
        }
        return lazy;
    }

    class MultiClassReaderItr implements Iterator<ClazzDefinition> {

        private ClazzDefinitionVisitor reuseVisit;

        @Override
        public boolean hasNext() {
            return pos < multiClassData.length;
        }

        @Override
        public ClazzDefinition next() {
            ClazzDefinitionVisitor visitor = null;
            if (this.reuseVisit == null) {
                if ((visitor = visitorFactory.creatClazzVisitor()).isReuse()) {
                    this.reuseVisit = visitor;
                }
            } else {
                visitor = this.reuseVisit;
            }
            ClassReader classReader = new ClassReader(multiClassData, pos);
            pos = classReader.accept(visitor, 0);
            return visitor.getDefinition();
        }
    }
}
