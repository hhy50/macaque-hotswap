package six.eared.macaque.agent.asm2;


import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class ClassFieldUniqueDesc {

    /**
     * className
     */
    private String className;

    /**
     * 字段名
     */
    private String name;

    /**
     * 字段类型
     */
    private String type;

    public ClassFieldUniqueDesc(String className, String name, String type) {
        this.className = className;
        this.name = name;
        this.type = type;
    }

    public static ClassFieldUniqueDesc of(String className, String name, String type) {
        // TODO cache
        return new ClassFieldUniqueDesc(className, name, type);
    }
}
