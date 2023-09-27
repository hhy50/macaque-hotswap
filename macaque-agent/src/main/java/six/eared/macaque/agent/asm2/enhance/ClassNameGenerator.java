package six.eared.macaque.agent.asm2.enhance;

public interface ClassNameGenerator {

    /**
     *
     * @param className
     * @param methodName
     * @return
     */
    public String generate(String className, String methodName);

    public String generateInnerAccessorName(String className);
}
