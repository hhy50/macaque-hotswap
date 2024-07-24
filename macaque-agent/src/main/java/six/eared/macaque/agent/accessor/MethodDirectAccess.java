package six.eared.macaque.agent.accessor;


import lombok.Data;

@Data
public class MethodDirectAccess implements AccessorRule {

    public static final AccessorRule INSTANCE = new MethodDirectAccess();
}
