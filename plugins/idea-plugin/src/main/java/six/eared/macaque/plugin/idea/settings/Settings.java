package six.eared.macaque.plugin.idea.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import six.eared.macaque.plugin.idea.PluginInfo;

import java.util.Objects;

@State(name = PluginInfo.ID, storages = {@Storage(PluginInfo.CONFIG_STORE)})
public class Settings implements PersistentStateComponent<Settings> {

    public String macaqueServerHost;

    public String macaqueServerPort;

    public boolean compatibilityMode;

    private static final Settings CURRENT = new Settings();

    public static Settings getCurrent() {
        return CURRENT;
    }

    public synchronized static void reset(Settings settings) {
        XmlSerializerUtil.copyBean(settings, CURRENT);
    }

    public String getMacaqueServerHost() {
        return macaqueServerHost;
    }

    public void setMacaqueServerHost(String macaqueServerHost) {
        this.macaqueServerHost = macaqueServerHost;
    }

    public String getMacaqueServerPort() {
        return macaqueServerPort;
    }

    public void setMacaqueServerPort(String macaqueServerPort) {
        this.macaqueServerPort = macaqueServerPort;
    }

    public boolean isCompatibilityMode() {
        return compatibilityMode;
    }

    public void setCompatibilityMode(boolean compatibilityMode) {
        this.compatibilityMode = compatibilityMode;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Settings settings = (Settings) o;
        return Objects.equals(macaqueServerHost, settings.macaqueServerHost)
                && Objects.equals(macaqueServerPort, settings.macaqueServerPort)
                && compatibilityMode == settings.compatibilityMode;
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

    @Override
    public @Nullable Settings getState() {
        System.out.println("getState");
        return getCurrent();
    }

    @Override
    public void loadState(@NotNull Settings state) {
        System.out.println("loadState");
        reset(state);
    }
}
