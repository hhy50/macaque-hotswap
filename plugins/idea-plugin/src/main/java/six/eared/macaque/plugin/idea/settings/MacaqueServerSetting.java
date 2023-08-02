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

    private SettingsUI ui = new SettingsUI();

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
        return ui.showPanel();
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() throws ConfigurationException {
    }
}
