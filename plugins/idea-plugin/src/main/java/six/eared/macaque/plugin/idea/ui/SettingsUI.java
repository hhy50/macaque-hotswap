package six.eared.macaque.plugin.idea.ui;


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

    public SettingsUI() {
        UiUtil.addGroup(panelContainer, "Main", (inner) -> {
            serverHostTextField = addInputBox(inner, "Server Host");
            serverPortTextField = addInputBox(inner, "Server Port");
        });

        UiUtil.addGroup(panelContainer, "Beta", (inner) -> {
            compatibilityModeCheckBox = addSelectBox(inner, "兼容模式");
        });
        UiUtil.fillY(panelContainer);
    }

    public JPanel showPanel() {
        return panelContainer;
    }

    public Settings getPanelConfig() {
        Settings settings = new Settings();
        settings.setMacaqueServerHost(serverHostTextField.getText());
        settings.setMacaqueServerPort(serverPortTextField.getText());
        settings.setCompatibilityMode(compatibilityModeCheckBox.isSelected());
        return settings;
    }
}
