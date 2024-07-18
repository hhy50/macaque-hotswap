package six.eared.macaque.agent.enhance;

import lombok.Getter;
import lombok.Setter;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;

public class ClazzDataDefinition extends ClazzDefinition {

    @Setter
    @Getter
    private byte[] bytecode;
}
