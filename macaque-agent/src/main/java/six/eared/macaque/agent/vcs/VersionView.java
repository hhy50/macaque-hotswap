package six.eared.macaque.agent.vcs;

import six.eared.macaque.agent.definition.Definition;
import six.eared.macaque.agent.enums.VersionViewStatus;

import java.util.ArrayList;
import java.util.List;


/**
 *
 */
public class VersionView {

    private VersionViewStatus status;

    private VersionDescriptor version;

    private List<Definition> definitions;

    VersionView() {
        this.status = VersionViewStatus.ACTIVE;
    }

    public VersionDescriptor getVersion() {
        return this.version;
    }

    public void setVersion(VersionDescriptor version) {
        this.version = version;
    }

    public List<Definition> getDefinitions() {
        return this.definitions;
    }

    public void setDefinitions(List<Definition> definitions) {
        this.definitions = definitions;
    }

    public void addDefinition(Definition definition) {
        if (this.definitions == null) {
            this.definitions = new ArrayList<>();
        }
        if (this.definitions.stream().noneMatch(item -> item.getName().equals(definition.getName()))) {
            this.definitions.add(definition);
        }
    }

    public VersionViewStatus getStatus() {
        return status;
    }

    public void setStatus(VersionViewStatus status) {
        this.status = status;
    }
}
