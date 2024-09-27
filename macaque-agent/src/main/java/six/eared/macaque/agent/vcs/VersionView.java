package six.eared.macaque.agent.vcs;

import lombok.Data;
import six.eared.macaque.agent.definition.Definition;
import six.eared.macaque.agent.enums.VersionViewStatus;

import java.util.ArrayList;
import java.util.List;


/**
 *
 */
@Data
public class VersionView {

    private VersionViewStatus status;

    private VersionDescriptor version;

    private List<Definition> definitions;

    VersionView() {
        this.status = VersionViewStatus.ACTIVE;
    }

    public void addDefinition(Definition definition) {
        if (this.definitions == null) {
            this.definitions = new ArrayList<>();
        }
        if (this.definitions.stream().noneMatch(item -> item.getName().equals(definition.getName()))) {
            this.definitions.add(definition);
        }
    }
}
