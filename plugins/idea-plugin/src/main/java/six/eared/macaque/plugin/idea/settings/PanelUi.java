package six.eared.macaque.plugin.idea.settings;

import com.intellij.ui.scale.JBUIScale;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class PanelUi {

    public static JPanel create(int size) {
        LC lc = new LC();
        lc.fill();
        lc.gridGap(JBUIScale.scale(size)+"px", "0")
                .insets("0");
        return new JPanel(new MigLayout(lc));
    }
}
