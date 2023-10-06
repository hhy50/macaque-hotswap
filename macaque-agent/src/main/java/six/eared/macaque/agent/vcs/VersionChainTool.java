package six.eared.macaque.agent.vcs;

import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.definition.Definition;
import six.eared.macaque.agent.enums.VersionViewStatus;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.agent.exceptions.VcsException;
import six.eared.macaque.common.util.CollectionUtil;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class VersionChainTool {

    private static final AtomicReference<VersionView> CURRENT_EPOCH = new AtomicReference<>();

    private static final VersionChain VERSION_CHAIN = new VersionChain();

    public static ClazzDefinition findLastClassVersion(String className, boolean dirtyRead) {
        if (dirtyRead && inActiveVersionView()) {
            List<Definition> definitions = CURRENT_EPOCH.get().getDefinitions();
            if (CollectionUtil.isNotEmpty(definitions)) {
                Optional<ClazzDefinition> any = definitions.stream()
                        .filter(item -> item.getName().equals(className))
                        .map(ClazzDefinition.class::cast)
                        .findAny();
                if (any.isPresent()) {
                    return any.get();
                }
            }
        }
        if (Environment.isOpenVersionControl()) {
            VersionDescriptor lastVd = VERSION_CHAIN.findLastVersion(className);
            if (lastVd != null) {
                VersionView versionView = VERSION_CHAIN.find(lastVd);
                Optional<ClazzDefinition> any = versionView.getDefinitions().stream()
                        .filter(item -> item.getName().equals(className))
                        .map(ClazzDefinition.class::cast)
                        .findAny();
                if (any.isPresent()) {
                    return any.get();
                }
            }
        }
        return null;
    }

    public static VersionView startNewEpoch() {
        if (inActiveVersionView()) {
            stopActiveVersionView(true);
        }
        VersionView versionView = new VersionView();
        if (CURRENT_EPOCH.compareAndSet(null, versionView)) {
            versionView.setVersion(VersionDescriptor.incrementVersion());
            return versionView;
        }
        VersionView unfinished = CURRENT_EPOCH.get();
        throw new VcsException("There are unfinished version view, version=" + unfinished.getVersion());
    }

    public static boolean inActiveVersionView() {
        VersionView versionView = CURRENT_EPOCH.get();
        return versionView != null
                && versionView.getStatus() == VersionViewStatus.ACTIVE.getStatus();
    }

    public static void stopActiveVersionView(boolean discard) {
        VersionView versionView = CURRENT_EPOCH.get();
        CURRENT_EPOCH.compareAndSet(versionView, null);
        if (!discard) {
            versionView.setStatus(VersionViewStatus.HISTORY.getStatus());
            putLastVersion(versionView);
        }
    }

    private static void putLastVersion(VersionView versionView) {
        if (Environment.isOpenVersionControl()) {
            VERSION_CHAIN.put(versionView);
        }
    }

    public static VersionView getActiveVersionView() {
        VersionView versionView = CURRENT_EPOCH.get();
        if (versionView != null && versionView.getStatus() == VersionViewStatus.ACTIVE.getStatus()) {
            return versionView;
        }
        throw new VcsException("No active version view");
    }
}
