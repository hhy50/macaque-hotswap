package six.eared.macaque.agent.enhance;

public class AccessorClassNameGenerator {

    /**
     * 生成访问器的类名
     * @param className
     * @return
     */
    public String generate(String className)  {
        return className + "$macaque$Accessor";
    }
}
