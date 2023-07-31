package six.eared.macaque.plugin.idea.settings;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.util.NlsContexts;
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
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return PluginInfo.NAME;
    }

    @Override
    public @Nullable JComponent createComponent() {
        return ui.showPanel();
    }

    @Override
    public boolean isModified() {
        System.out.println("isModified");
        return true;
    }

    @Override
    public void apply() throws ConfigurationException {
        System.out.println("apply");
    }
}
