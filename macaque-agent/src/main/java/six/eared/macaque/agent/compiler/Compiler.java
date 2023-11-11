package six.eared.macaque.agent.compiler;

import six.eared.macaque.agent.exceptions.MemoryCompileException;

import java.util.List;
import java.util.Map;

public interface Compiler {

    /**
     * @return class bytecode
     */
    List<byte[]> compile(Map<String, byte[]> sourceCodes) throws MemoryCompileException;

}
