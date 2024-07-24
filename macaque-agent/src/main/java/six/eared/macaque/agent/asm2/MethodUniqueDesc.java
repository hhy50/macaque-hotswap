package six.eared.macaque.agent.asm2;


import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class MethodUniqueDesc {

    /**
     * 方法名字
     */
    private String name;

    /**
     * 方法描述
     */
    private String desc;

    public MethodUniqueDesc(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public static MethodUniqueDesc of(String name, String desc) {
        return new MethodUniqueDesc(name, desc);
    }
}
