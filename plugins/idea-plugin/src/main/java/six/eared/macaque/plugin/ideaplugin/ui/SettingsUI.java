package six.eared.macaque.plugin.ideaplugin.ui;


import javax.swing.*;

import static six.eared.macaque.plugin.ideaplugin.ui.UiUtil.*;


public class SettingsUI {

    private final JPanel panelContainer = new JPanel(createMigLayoutVertical());

    public SettingsUI() {
        UiUtil.addGroup(panelContainer, "Main", (inner) -> {
            addInputBox(inner, "Server Host", null);
            addInputBox(inner, "Server Port", null);
        });

        UiUtil.addGroup(panelContainer, "Beta", (inner) -> {
            addSelectBox(inner, "兼容模式");
        });
        UiUtil.fillY(panelContainer);
    }

    public JPanel showPanel() {
        return panelContainer;
    }
}
