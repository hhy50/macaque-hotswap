package six.eared.macaque.agent.spi;


import lombok.Data;

@Data
public class LibraryDefinition {

    private String name;

    private Class<?> clazz;

    @Override
    public String toString() {
        return "LibraryDefinition{" +
                "name='" + name + '\'' +
                ", clazz='" + clazz + '\'' +
                '}';
    }
}
