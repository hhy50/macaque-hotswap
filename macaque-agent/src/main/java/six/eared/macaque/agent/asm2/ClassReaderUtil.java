package six.eared.macaque.agent.asm2;

import org.objectweb.asm.ClassReader;

public class ClassReaderUtil {

    /**
     * https://gitlab.ow2.org/asm/asm/-/merge_requests/405
     *
     * @param cr
     * @return
     */
    public static int getEndOffset(ClassReader cr) {
        int currentOffset = cr.header;
        int interfaceCount = cr.readUnsignedShort(currentOffset+6);
        currentOffset += 8+2*interfaceCount;
        int fieldsCount = cr.readUnsignedShort(currentOffset);
        currentOffset += 2;
        while (fieldsCount-- > 0) {
            currentOffset = getEndAttributesOffset(cr, currentOffset+6);
        }
        int methodsCount = cr.readUnsignedShort(currentOffset);
        currentOffset += 2;
        while (methodsCount-- > 0) {
            currentOffset = getEndAttributesOffset(cr, currentOffset+6);
        }
        return getEndAttributesOffset(cr, currentOffset);
    }

    static int getEndAttributesOffset(ClassReader cr, int offset) {
        int attributesCount = cr.readUnsignedShort(offset);
        offset += 2;
        while (attributesCount-- > 0) {
            offset += 6+cr.readInt(offset+2);
        }
        return offset;
    }
}
