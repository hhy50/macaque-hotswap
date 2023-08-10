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
import six.eared.macaque.plugin.idea.thread.Executors;
import six.eared.macaque.plugin.idea.ui.SettingsUI;

import javax.swing.*;

public class MacaqueServerSetting implements SearchableConfigurable, Configurable.VariableProjectAppLevel {

    private Project project;

    private SettingsUI settingsUI;

    public MacaqueServerSetting(@NotNull Project project) {
        this.project = project;
        this.settingsUI = new SettingsUI();

        Settings settings = Settings.getInstance(project);
        if (settings != null) {
            this.settingsUI.initValue(settings.getState());
        }
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
        Settings.State panelConfig = settingsUI.getPanelConfig();
        Settings settings = Settings.getInstance(project);
        if (settings != null) {
            return !settings.getState().equals(panelConfig);
        }

        return panelConfig != null;
    }

    @Override
    public void apply() {
        Settings.cover(project, settingsUI.getPanelConfig());
        Executors.submit(() -> JpsHolder.refresh(project));
    }

    @Override
    public void reset() {
        Settings settings = Settings.getInstance(project);
        this.settingsUI.initValue(settings.getState());
    }

    @Override
    public boolean isProjectLevel() {
        return true;
    }
}
