package six.eared.macaque.plugin.idea.settings;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import six.eared.macaque.plugin.idea.PluginInfo;
import six.eared.macaque.plugin.idea.ui.SettingsUI;

import javax.swing.*;

public class MacaqueServerSetting implements SearchableConfigurable {

    private SettingsUI settingsUI = new SettingsUI();

    @Override
    public @NotNull @NonNls String getId() {
        return PluginInfo.ID;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return PluginInfo.NAME;
    }

    @Override
    public @Nullable JComponent createComponent() {
        return settingsUI.showPanel();
    }

    @Override
    public boolean isModified() {
        return !Settings.getCurrent()
                .equals(settingsUI.getPanelConfig());
    }

    @Override
    public void apply() {
        Settings.reset(settingsUI.getPanelConfig());
    }
}
