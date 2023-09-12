package six.eared.macaque.agent.spi;


public class LibraryDefinition {

    private String name;

    private Class<?> clazz;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public String toString() {
        return "LibraryDefinition{" +
                "name='" + name + '\'' +
                ", clazz='" + clazz + '\'' +
                '}';
    }
}
