package six.eared.macaque.plugin.idea.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import six.eared.macaque.plugin.idea.PluginInfo;
import six.eared.macaque.plugin.idea.jps.JpsHolder;
import six.eared.macaque.plugin.idea.ui.SettingsUI;

import javax.swing.*;

public class MacaqueServerSetting implements SearchableConfigurable, Configurable.VariableProjectAppLevel {

    private Project project;

    private SettingsUI settingsUI;

    public MacaqueServerSetting(@NotNull Project project) {
        this.project = project;
        this.settingsUI = new SettingsUI();
        this.settingsUI.initValue(Settings.getInstance(project).getState());
    }

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
        return !Settings.getInstance(project).getState()
                .equals(settingsUI.getPanelConfig());
    }

    @Override
    public void apply() {
        Settings.cover(project, settingsUI.getPanelConfig());
        JpsHolder.refresh(project);
    }

    @Override
    public boolean isProjectLevel() {
        return true;
    }
}
