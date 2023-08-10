package six.eared.macaque.plugin.idea.settings;

import com.intellij.notification.*;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import six.eared.macaque.plugin.idea.PluginInfo;
import six.eared.macaque.plugin.idea.notify.NotifyGroupName;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@State(name = PluginInfo.SERVER_CONFIG_ID)
public class Settings implements PersistentStateComponent<Settings.State> {

    private static final Map<Project, State> PROJECT_STATE = new HashMap<>();

    private static final NotificationGroup NOTIFY_GROUP = NotificationGroupManager.getInstance()
            .getNotificationGroup(NotifyGroupName.BALLOON);

    private Project project;

    public Settings(@NotNull Project project) {
        this.project = project;
    }

    public static Settings getInstance(Project project) {
        Settings settings = project.getService(Settings.class);
        if (settings.getState() == null || !settings.getState().checkRequired()) {
            Notification notification = NOTIFY_GROUP.createNotification("Not configuration macaque server",
                    NotificationType.ERROR);
            Notifications.Bus.notify(notification);
            return null;
        }
        return settings;
    }

    @Override
    public @Nullable State getState() {
        Settings.State state = PROJECT_STATE.get(project);
        if (state == null) {
            state = new Settings.State();
            PROJECT_STATE.put(project, state);
        }
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        cover(project, state);
    }

    public synchronized static void cover(Project project, Settings.State state) {
        PROJECT_STATE.put(project, state);
    }

    public static class State implements StateCheck {

        @Required
        public String macaqueServerHost;

        @Required
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

        public String getUrl() {
            String scheme = "";
            if (!macaqueServerHost.startsWith("http")) {
                scheme = "http://";
            }
            return scheme + macaqueServerHost + ":" + macaqueServerPort;
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
