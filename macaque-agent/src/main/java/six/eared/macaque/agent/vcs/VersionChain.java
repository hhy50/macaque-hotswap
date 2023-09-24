package six.eared.macaque.agent.vcs;

import six.eared.macaque.agent.definition.Definition;
import six.eared.macaque.common.type.FileType;

import java.util.*;

public class VersionChain {

    private TreeMap<VersionDescriptor, VersionView> versionViews =
            new TreeMap<>(new VersionDescriptor.VersionComparator());

    private Map<String, List<VersionDescriptor>> fileNameIndex =
            new HashMap<>();

    VersionDescriptor findLastVersion(String fileName) {
        List<VersionDescriptor> versionDescriptors = fileNameIndex.get(fileName);
        return versionDescriptors != null
                ? versionDescriptors.get(versionDescriptors.size() - 1)
                : null;
    }

    VersionView find(VersionDescriptor vd) {
        return versionViews.get(vd);
    }

    void put(VersionView versionView) {
        for (Definition definition : versionView.getDefinitions()) {
            if (FileType.Class.match(definition.getFileType())) {
                fileNameIndex.compute(definition.getName(), (fileName, versionDescriptors) -> {
                    if (versionDescriptors == null) {
                        versionDescriptors = new ArrayList<>();
                    }
                    versionDescriptors.add(versionView.getVersion());
                    return versionDescriptors;
                });
            }
        }
        versionViews.put(versionView.getVersion(), versionView);
    }
}
