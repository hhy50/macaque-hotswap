package six.eared.macaque.plugin.idea.ui;


import com.intellij.openapi.project.Project;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.components.JBCheckBox;
import six.eared.macaque.plugin.idea.settings.Settings;

import javax.swing.*;

import static six.eared.macaque.plugin.idea.ui.UiUtil.*;


public class SettingsUI {

    private final JPanel panelContainer = new JPanel(createMigLayoutVertical());

    private EditorTextField serverHostTextField;

    private EditorTextField serverPortTextField;

    private JBCheckBox compatibilityModeCheckBox;

    private Project project;

    public SettingsUI(Project project) {
        this.project = project;
        UiUtil.addGroup(panelContainer, "Main", (inner) -> {
            serverHostTextField = addInputBox(inner, "Server Host");
            serverPortTextField = addInputBox(inner, "Server Port");
        });

        UiUtil.addGroup(panelContainer, "Beta", (inner) -> {
            compatibilityModeCheckBox = addSelectBox(inner, "兼容模式");
        });
        UiUtil.fillY(panelContainer);

        initValue();
    }

    private void initValue() {
        Settings.State state = Settings.getInstance(project).getState();
        if (state != null) {
            serverHostTextField.setText(state.macaqueServerHost);
            serverPortTextField.setText(state.macaqueServerPort);
            compatibilityModeCheckBox.setSelected(state.compatibilityMode);
        }
    }

    public JPanel showPanel() {
        return panelContainer;
    }

    public Settings.State getPanelConfig() {
        Settings.State settings = new Settings.State();
        settings.macaqueServerHost = serverHostTextField.getText();
        settings.macaqueServerPort = serverPortTextField.getText();
        settings.compatibilityMode = compatibilityModeCheckBox.isSelected();
        return settings;
    }
}
