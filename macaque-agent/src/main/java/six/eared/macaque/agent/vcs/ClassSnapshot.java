package six.eared.macaque.agent.vcs;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClassSnapshot {
    private String className;
    private byte[] originByteCode;
    private String enhancedByteCode;
}
