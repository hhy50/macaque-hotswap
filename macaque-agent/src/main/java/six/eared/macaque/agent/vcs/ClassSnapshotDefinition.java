package six.eared.macaque.agent.vcs;


import lombok.Data;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.definition.Definition;

@Data
public class ClassSnapshotDefinition implements Definition {

    private final ClazzDefinition clazzDefinition;
    /**
     * 增强后的字节码
     */
    private final byte[] enhancedByteCode;

    /**
     *
     */
    public ClassSnapshotDefinition(ClazzDefinition definition, byte[] enhancedByteCode) {
        this.clazzDefinition = definition;
        this.enhancedByteCode = enhancedByteCode;
    }

    @Override
    public String getName() {
        return this.clazzDefinition.getName();
    }

    @Override
    public String getFileType() {
        return "class";
    }

    @Override
    public byte[] getByteArray() {
        return enhancedByteCode;
    }
}
