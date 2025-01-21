package six.eared.macaque.preload;


import lombok.Data;

@Data
public class PatchedMethod {
    private String name;
    private Class<?> type;
}