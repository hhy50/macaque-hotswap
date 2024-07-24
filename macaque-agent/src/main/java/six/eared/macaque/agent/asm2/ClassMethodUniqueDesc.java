package six.eared.macaque.agent.asm2;


import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class ClassMethodUniqueDesc {

    /**
     * className
     */
    private String className;

    /**
     * 方法名字
     */
    private String name;

    /**
     * 方法描述
     */
    private String desc;

    public ClassMethodUniqueDesc(String className, String name, String desc) {
        this.className = className;
        this.name = name;
        this.desc = desc;
    }

    public static ClassMethodUniqueDesc of(String className, String name, String desc) {
        // TODO cache
        return new ClassMethodUniqueDesc(className, name, desc);
    }
}
