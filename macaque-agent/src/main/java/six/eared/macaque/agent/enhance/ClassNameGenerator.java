package six.eared.macaque.agent.enhance;

public interface ClassNameGenerator {

    /**
     *
     * @param className
     * @param methodName
     * @return
     */
    public String generate(String className, String methodName);

    public String generateAccessorName(String className);
}
