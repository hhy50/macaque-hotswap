package six.eared.macaque.agent.vcs;

import six.eared.macaque.agent.asm2.classes.ClazzDefinition;

import java.util.List;


/**
 *
 */
public class VersionView {

    private VersionDescriptor version;

    private List<ClazzDefinition> definitions;

    public VersionDescriptor getVersion() {
        return version;
    }

    public void setVersion(VersionDescriptor version) {
        this.version = version;
    }

    public List<ClazzDefinition> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(List<ClazzDefinition> definitions) {
        this.definitions = definitions;
    }
}
