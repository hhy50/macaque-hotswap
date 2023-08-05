package six.eared.macaque.plugin.idea.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import six.eared.macaque.plugin.idea.PluginInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@State(name = PluginInfo.ID)
public class Settings implements PersistentStateComponent<Settings.State> {

    private static final Map<Project, State> PROJECT_STATE = new HashMap<>();

    private Project project;

    public Settings(@NotNull Project project) {
        this.project = project;
    }

    public static Settings getInstance(Project project) {
        return project.getService(Settings.class);
    }

    @Override
    public @Nullable State getState() {
        return PROJECT_STATE.get(project);
    }

    @Override
    public void loadState(@NotNull State state) {
        cover(project, state);
    }

    public synchronized static void cover(Project project, Settings.State state) {
        PROJECT_STATE.put(project, state);
    }

    public static class State {
        public String macaqueServerHost;

        public String macaqueServerPort;

        public boolean compatibilityMode;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            State state = (State) o;
            return Objects.equals(macaqueServerHost, state.macaqueServerHost)
                    && Objects.equals(macaqueServerPort, state.macaqueServerPort)
                    && compatibilityMode == state.compatibilityMode;
        }

        @Override
        public int hashCode() {
            return Objects.hash(macaqueServerHost, macaqueServerPort, compatibilityMode);
        }

        @Override
        public String toString() {
            return "Settings{" +
                    "macaqueServerHost='" + macaqueServerHost + '\'' +
                    ", macaqueServerPort='" + macaqueServerPort + '\'' +
                    ", compatibilityMode=" + compatibilityMode +
                    '}';
        }
    }
}
