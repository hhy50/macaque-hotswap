package six.eared.macaque.agent.compiler;

import java.util.List;
import java.util.Map;

public interface Compiler {


    /**
     *
     * @return class bytecode
     */
    public List<byte[]> compile(Map<String, String> sourceCodes);
}
